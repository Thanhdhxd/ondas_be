package com.example.ondas_be.application.service.impl;

import com.example.ondas_be.application.dto.request.SearchFilterRequest;
import com.example.ondas_be.application.dto.response.AlbumResponse;
import com.example.ondas_be.application.dto.response.ArtistSummaryResponse;
import com.example.ondas_be.application.dto.response.GenreSummaryResponse;
import com.example.ondas_be.application.dto.response.SearchResponse;
import com.example.ondas_be.application.dto.response.SearchSuggestionResponse;
import com.example.ondas_be.application.dto.response.SongResponse;
import com.example.ondas_be.application.exception.UserNotFoundException;
import com.example.ondas_be.application.mapper.AlbumMapper;
import com.example.ondas_be.application.mapper.ArtistMapper;
import com.example.ondas_be.application.mapper.GenreMapper;
import com.example.ondas_be.application.mapper.SongMapper;
import com.example.ondas_be.application.service.port.SearchServicePort;
import com.example.ondas_be.domain.entity.Album;
import com.example.ondas_be.domain.entity.Artist;
import com.example.ondas_be.domain.entity.Genre;
import com.example.ondas_be.domain.entity.SearchHistory;
import com.example.ondas_be.domain.entity.Song;
import com.example.ondas_be.domain.entity.User;
import com.example.ondas_be.domain.repoport.AlbumArtistRepoPort;
import com.example.ondas_be.domain.repoport.AlbumRepoPort;
import com.example.ondas_be.domain.repoport.ArtistRepoPort;
import com.example.ondas_be.domain.repoport.GenreRepoPort;
import com.example.ondas_be.domain.repoport.SearchHistoryRepoPort;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService implements SearchServicePort {

    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 50;
    private static final int SUGGESTION_LIMIT = 10;
    private static final int TRENDING_DAYS = 7;

    private final SongRepoPort songRepoPort;
    private final ArtistRepoPort artistRepoPort;
    private final AlbumRepoPort albumRepoPort;
    private final GenreRepoPort genreRepoPort;
    private final SongArtistRepoPort songArtistRepoPort;
    private final SongGenreRepoPort songGenreRepoPort;
    private final AlbumArtistRepoPort albumArtistRepoPort;
    private final SearchHistoryRepoPort searchHistoryRepoPort;
    private final UserRepoPort userRepoPort;
    private final SongMapper songMapper;
    private final ArtistMapper artistMapper;
    private final AlbumMapper albumMapper;
    private final GenreMapper genreMapper;

    @Override
    @Transactional(readOnly = true)
    public SearchResponse search(SearchFilterRequest filter) {
        String query = normalizeQuery(filter.getQuery());
        int page = Math.max(0, filter.getPage());
        int size = Math.min(filter.getSize() > 0 ? filter.getSize() : DEFAULT_SIZE, MAX_SIZE);

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

    @Override
    @Transactional(readOnly = true)
    public SearchSuggestionResponse getSuggestions(String userEmail) {
        User user = userRepoPort.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userEmail));

        // Lịch sử tìm kiếm của user, loại bỏ trùng lặp giữ thứ tự gần nhất
        List<String> recentSearches = searchHistoryRepoPort
                .findRecentByUserId(user.getId(), SUGGESTION_LIMIT * 2)
                .stream()
                .map(SearchHistory::getQuery)
                .distinct()
                .limit(SUGGESTION_LIMIT)
                .toList();

        // Trending toàn hệ thống trong 7 ngày qua
        List<String> trendingSearches = searchHistoryRepoPort
                .findTrendingQueries(SUGGESTION_LIMIT, TRENDING_DAYS);

        // Top bài hát theo lượt nghe
        List<Song> trendingSongs = songRepoPort.findTopByPlayCount(SUGGESTION_LIMIT);
        List<SongResponse> trendingSongResponses = buildSongResponses(trendingSongs);

        // Tất cả thể loại
        List<Genre> genres = genreRepoPort.findAll();

        return SearchSuggestionResponse.builder()
                .recentSearches(recentSearches)
                .trendingSearches(trendingSearches)
                .trendingSongs(trendingSongResponses)
                .genres(genreMapper.toResponseList(genres))
                .build();
    }

    @Override
    @Transactional
    public void saveSearchHistory(String query, String userEmail) {
        User user = userRepoPort.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userEmail));

        // Xoá entry cũ cùng query để tránh trùng lặp, sau đó thêm mới (đẩy lên đầu)
        searchHistoryRepoPort.deleteByUserIdAndQuery(user.getId(), query.trim());
        searchHistoryRepoPort.save(new SearchHistory(null, user.getId(), query.trim(), null));
    }

    @Override
    @Transactional
    public void clearSearchHistory(String userEmail) {
        User user = userRepoPort.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userEmail));
        searchHistoryRepoPort.deleteAllByUserId(user.getId());
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
