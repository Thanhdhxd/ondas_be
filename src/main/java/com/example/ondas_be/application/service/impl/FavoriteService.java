package com.example.ondas_be.application.service.impl;

import com.example.ondas_be.application.dto.common.PageResultDto;
import com.example.ondas_be.application.dto.response.ArtistSummaryResponse;
import com.example.ondas_be.application.dto.response.FavoriteSongResponse;
import com.example.ondas_be.application.dto.response.GenreSummaryResponse;
import com.example.ondas_be.application.exception.FavoriteAlreadyExistsException;
import com.example.ondas_be.application.exception.FavoriteNotFoundException;
import com.example.ondas_be.application.exception.SongNotFoundException;
import com.example.ondas_be.application.exception.UserNotFoundException;
import com.example.ondas_be.application.mapper.ArtistMapper;
import com.example.ondas_be.application.mapper.GenreMapper;
import com.example.ondas_be.application.service.port.FavoriteServicePort;
import com.example.ondas_be.domain.entity.Artist;
import com.example.ondas_be.domain.entity.Favorite;
import com.example.ondas_be.domain.entity.Genre;
import com.example.ondas_be.domain.entity.Song;
import com.example.ondas_be.domain.entity.User;
import com.example.ondas_be.domain.repoport.ArtistRepoPort;
import com.example.ondas_be.domain.repoport.FavoriteRepoPort;
import com.example.ondas_be.domain.repoport.GenreRepoPort;
import com.example.ondas_be.domain.repoport.SongArtistRepoPort;
import com.example.ondas_be.domain.repoport.SongGenreRepoPort;
import com.example.ondas_be.domain.repoport.SongRepoPort;
import com.example.ondas_be.domain.repoport.UserRepoPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteService implements FavoriteServicePort {

    private final FavoriteRepoPort favoriteRepoPort;
    private final UserRepoPort userRepoPort;
    private final SongRepoPort songRepoPort;
    private final SongArtistRepoPort songArtistRepoPort;
    private final SongGenreRepoPort songGenreRepoPort;
    private final ArtistRepoPort artistRepoPort;
    private final GenreRepoPort genreRepoPort;
    private final ArtistMapper artistMapper;
    private final GenreMapper genreMapper;

    @Override
    @Transactional
    public void addFavorite(String email, UUID songId) {
        User user = resolveUser(email);

        Song song = songRepoPort.findById(songId)
                .orElseThrow(() -> new SongNotFoundException("Song not found with id: " + songId));
        if (!song.isActive()) {
            throw new SongNotFoundException("Song not found with id: " + songId);
        }

        if (favoriteRepoPort.exists(user.getId(), songId)) {
            throw new FavoriteAlreadyExistsException("Song is already in favorites");
        }

        favoriteRepoPort.add(user.getId(), songId);
    }

    @Override
    @Transactional
    public void removeFavorite(String email, UUID songId) {
        User user = resolveUser(email);

        if (!favoriteRepoPort.exists(user.getId(), songId)) {
            throw new FavoriteNotFoundException("Song is not in favorites");
        }

        favoriteRepoPort.remove(user.getId(), songId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isFavorite(String email, UUID songId) {
        User user = resolveUser(email);
        return favoriteRepoPort.exists(user.getId(), songId);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResultDto<FavoriteSongResponse> getFavorites(String email, int page, int size) {
        User user = resolveUser(email);

        List<Favorite> favorites = favoriteRepoPort.findByUserId(user.getId(), page, size);
        long total = favoriteRepoPort.countByUserId(user.getId());

        if (favorites.isEmpty()) {
            return buildEmptyPage(page, size);
        }

        // Batch-fetch songs để tránh N+1 queries
        List<UUID> songIds = favorites.stream().map(Favorite::getSongId).toList();
        Map<UUID, Song> songMap = songRepoPort.findByIds(songIds).stream()
                .collect(Collectors.toMap(Song::getId, Function.identity()));

        // Batch-fetch artists và genres
        Map<UUID, List<UUID>> artistIdsBySong = songArtistRepoPort.findArtistIdsBySongIds(songIds);
        Map<UUID, List<Long>> genreIdsBySong = songGenreRepoPort.findGenreIdsBySongIds(songIds);

        List<UUID> allArtistIds = artistIdsBySong.values().stream()
                .flatMap(List::stream).distinct().toList();
        List<Long> allGenreIds = genreIdsBySong.values().stream()
                .flatMap(List::stream).distinct().toList();

        Map<UUID, ArtistSummaryResponse> artistById = artistRepoPort.findByIds(allArtistIds).stream()
                .collect(Collectors.toMap(Artist::getId, artistMapper::toSummaryResponse));
        Map<Long, GenreSummaryResponse> genreById = genreRepoPort.findByIds(allGenreIds).stream()
                .collect(Collectors.toMap(Genre::getId, genreMapper::toSummaryResponse));

        List<FavoriteSongResponse> items = favorites.stream()
                .map(fav -> {
                    Song song = songMap.get(fav.getSongId());
                    if (song == null) return null;

                    List<ArtistSummaryResponse> artists = artistIdsBySong
                            .getOrDefault(song.getId(), Collections.emptyList())
                            .stream().map(artistById::get).filter(Objects::nonNull).toList();

                    List<GenreSummaryResponse> genres = genreIdsBySong
                            .getOrDefault(song.getId(), Collections.emptyList())
                            .stream().map(genreById::get).filter(Objects::nonNull).toList();

                    return FavoriteSongResponse.builder()
                            .songId(song.getId())
                            .title(song.getTitle())
                            .slug(song.getSlug())
                            .durationSeconds(song.getDurationSeconds())
                            .audioUrl(song.getAudioUrl())
                            .audioFormat(song.getAudioFormat())
                            .coverUrl(song.getCoverUrl())
                            .playCount(song.getPlayCount())
                            .favoritedAt(fav.getCreatedAt())
                            .artists(artists)
                            .genres(genres)
                            .build();
                })
                .filter(Objects::nonNull)
                .toList();

        int totalPages = size > 0 ? (int) Math.ceil((double) total / size) : 0;

        return PageResultDto.<FavoriteSongResponse>builder()
                .items(items)
                .page(page)
                .size(size)
                .totalElements(total)
                .totalPages(totalPages)
                .build();
    }

    // ---- helpers ----

    private User resolveUser(String email) {
        return userRepoPort.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + email));
    }

    private PageResultDto<FavoriteSongResponse> buildEmptyPage(int page, int size) {
        return PageResultDto.<FavoriteSongResponse>builder()
                .items(Collections.emptyList())
                .page(page)
                .size(size)
                .totalElements(0)
                .totalPages(0)
                .build();
    }
}
