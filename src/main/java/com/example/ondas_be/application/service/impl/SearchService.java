package com.example.ondas_be.application.service.impl;

import com.example.ondas_be.application.dto.request.SearchFilterRequest;
import com.example.ondas_be.application.dto.response.AlbumResponse;
import com.example.ondas_be.application.dto.response.ArtistResponse;
import com.example.ondas_be.application.dto.response.ArtistSummaryResponse;
import com.example.ondas_be.application.dto.response.GenreSummaryResponse;
import com.example.ondas_be.application.dto.response.SearchResponse;
import com.example.ondas_be.application.dto.response.SongResponse;
import com.example.ondas_be.application.mapper.AlbumMapper;
import com.example.ondas_be.application.mapper.ArtistMapper;
import com.example.ondas_be.application.mapper.GenreMapper;
import com.example.ondas_be.application.mapper.SongMapper;
import com.example.ondas_be.application.service.port.SearchServicePort;
import com.example.ondas_be.domain.entity.Album;
import com.example.ondas_be.domain.entity.Artist;
import com.example.ondas_be.domain.entity.Genre;
import com.example.ondas_be.domain.entity.Song;
import com.example.ondas_be.domain.repoport.AlbumArtistRepoPort;
import com.example.ondas_be.domain.repoport.AlbumRepoPort;
import com.example.ondas_be.domain.repoport.ArtistRepoPort;
import com.example.ondas_be.domain.repoport.GenreRepoPort;
import com.example.ondas_be.domain.repoport.SongArtistRepoPort;
import com.example.ondas_be.domain.repoport.SongGenreRepoPort;
import com.example.ondas_be.domain.repoport.SongRepoPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService implements SearchServicePort {

    private static final int DEFAULT_SIZE = 10;

    private final SongRepoPort songRepoPort;
    private final ArtistRepoPort artistRepoPort;
    private final AlbumRepoPort albumRepoPort;
    private final GenreRepoPort genreRepoPort;
    private final SongArtistRepoPort songArtistRepoPort;
    private final SongGenreRepoPort songGenreRepoPort;
    private final AlbumArtistRepoPort albumArtistRepoPort;
    private final SongMapper songMapper;
    private final ArtistMapper artistMapper;
    private final AlbumMapper albumMapper;
    private final GenreMapper genreMapper;

    @Override
    @Transactional(readOnly = true)
    public SearchResponse search(SearchFilterRequest filter) {
        String query = normalizeQuery(filter.getQuery());
        int page = Math.max(0, filter.getPage());
        int size = filter.getSize() > 0 ? filter.getSize() : DEFAULT_SIZE;

        List<Song> songs = songRepoPort.findByTitleContains(query, page, size);
        long totalSongs = songRepoPort.countByTitleContains(query);

        List<Artist> artists = artistRepoPort.findByNameContains(query, page, size);
        long totalArtists = artistRepoPort.countByNameContains(query);

        List<Album> albums = albumRepoPort.findByTitleContains(query, page, size);
        long totalAlbums = albumRepoPort.countByTitleContains(query);

        return SearchResponse.builder()
                .query(query)
                .page(page)
                .size(size)
                .totalSongs(totalSongs)
                .totalArtists(totalArtists)
                .totalAlbums(totalAlbums)
                .songs(buildSongResponses(songs))
                .artists(artistMapper.toResponseList(artists))
                .albums(buildAlbumResponses(albums))
                .build();
    }

    private String normalizeQuery(String query) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Query is required");
        }
        return query.trim();
    }

    private List<SongResponse> buildSongResponses(List<Song> songs) {
        if (songs.isEmpty()) {
            return Collections.emptyList();
        }

        List<UUID> songIds = songs.stream().map(Song::getId).toList();
        Map<UUID, List<UUID>> artistIdsBySong = songArtistRepoPort.findArtistIdsBySongIds(songIds);
        Map<UUID, List<Long>> genreIdsBySong = songGenreRepoPort.findGenreIdsBySongIds(songIds);

        List<UUID> allArtistIds = artistIdsBySong.values().stream()
                .flatMap(List::stream)
                .distinct()
                .toList();
        List<Long> allGenreIds = genreIdsBySong.values().stream()
                .flatMap(List::stream)
                .distinct()
                .toList();

        Map<UUID, ArtistSummaryResponse> artistById = artistRepoPort.findByIds(allArtistIds).stream()
                .collect(Collectors.toMap(Artist::getId, artistMapper::toSummaryResponse));
        Map<Long, GenreSummaryResponse> genreById = genreRepoPort.findByIds(allGenreIds).stream()
                .collect(Collectors.toMap(Genre::getId, genreMapper::toSummaryResponse));

        return songs.stream().map(song -> {
            SongResponse response = songMapper.toResponse(song);
            response.setArtists(artistIdsBySong.getOrDefault(song.getId(), Collections.emptyList())
                    .stream()
                    .map(artistById::get)
                    .filter(Objects::nonNull)
                    .toList());
            response.setGenres(genreIdsBySong.getOrDefault(song.getId(), Collections.emptyList())
                    .stream()
                    .map(genreById::get)
                    .filter(Objects::nonNull)
                    .toList());
            return response;
        }).toList();
    }

    private List<AlbumResponse> buildAlbumResponses(List<Album> albums) {
        if (albums.isEmpty()) {
            return Collections.emptyList();
        }

        return albums.stream().map(album -> {
            AlbumResponse response = albumMapper.toResponse(album);
            response.setArtistIds(albumArtistRepoPort.findArtistIdsByAlbumId(album.getId()));
            response.setTracklist(Collections.emptyList());
            return response;
        }).toList();
    }
}
