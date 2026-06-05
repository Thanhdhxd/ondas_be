package com.example.ondas_be.application.service.impl;

import com.example.ondas_be.application.dto.response.AdminDailyPlayResponse;
import com.example.ondas_be.application.dto.response.AdminDauMauResponse;
import com.example.ondas_be.application.dto.response.AdminTopArtistResponse;
import com.example.ondas_be.application.dto.response.AdminTopSongResponse;
import com.example.ondas_be.application.dto.response.ArtistSummaryResponse;
import com.example.ondas_be.application.mapper.ArtistMapper;
import com.example.ondas_be.application.service.port.AdminStatsServicePort;
import com.example.ondas_be.domain.entity.Artist;
import com.example.ondas_be.domain.entity.ArtistPlayCount;
import com.example.ondas_be.domain.entity.DailyPlayCount;
import com.example.ondas_be.domain.entity.Song;
import com.example.ondas_be.domain.entity.SongPlayCount;
import com.example.ondas_be.domain.repoport.ArtistRepoPort;
import com.example.ondas_be.domain.repoport.SongArtistRepoPort;
import com.example.ondas_be.domain.repoport.SongRepoPort;
import com.example.ondas_be.domain.repoport.StatsRepoPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminStatsService implements AdminStatsServicePort {

    private static final int DEFAULT_LIMIT = 10;
    private static final int MAX_LIMIT = 100;
    private static final int DEFAULT_RANGE_DAYS = 30;

    private final StatsRepoPort statsRepoPort;
    private final SongRepoPort songRepoPort;
    private final SongArtistRepoPort songArtistRepoPort;
    private final ArtistRepoPort artistRepoPort;
    private final ArtistMapper artistMapper;

    @Override
    @Transactional(readOnly = true)
    public List<AdminTopSongResponse> getTopSongs(LocalDate from, LocalDate to, Integer limit) {
        DateRange range = resolveRange(from, to, DEFAULT_RANGE_DAYS);
        int resolvedLimit = resolveLimit(limit);

        List<SongPlayCount> topSongs = statsRepoPort.findTopSongs(
                range.getFromDateTime(), range.getToDateTimeExclusive(), resolvedLimit);
        if (topSongs.isEmpty()) {
            return Collections.emptyList();
        }

        List<UUID> songIds = topSongs.stream().map(SongPlayCount::getSongId).toList();
        Map<UUID, Song> songById = songRepoPort.findByIds(songIds).stream()
                .collect(Collectors.toMap(Song::getId, song -> song));

        Map<UUID, List<UUID>> artistIdsBySong = songArtistRepoPort.findArtistIdsBySongIds(songIds);
        List<UUID> allArtistIds = artistIdsBySong.values().stream()
                .flatMap(List::stream).distinct().toList();
        Map<UUID, ArtistSummaryResponse> artistById = artistRepoPort.findByIds(allArtistIds).stream()
                .collect(Collectors.toMap(Artist::getId, artistMapper::toSummaryResponse));

        return topSongs.stream()
                .map(item -> {
                    Song song = songById.get(item.getSongId());
                    if (song == null) {
                        return null;
                    }
                    List<ArtistSummaryResponse> artists = artistIdsBySong
                            .getOrDefault(song.getId(), Collections.emptyList()).stream()
                            .map(artistById::get)
                            .filter(java.util.Objects::nonNull)
                            .toList();
                    return AdminTopSongResponse.builder()
                            .id(song.getId())
                            .title(song.getTitle())
                            .coverUrl(song.getCoverUrl())
                            .playCount(item.getPlayCount())
                            .artists(artists)
                            .build();
                })
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminTopArtistResponse> getTopArtists(LocalDate from, LocalDate to, Integer limit) {
        DateRange range = resolveRange(from, to, DEFAULT_RANGE_DAYS);
        int resolvedLimit = resolveLimit(limit);

        List<ArtistPlayCount> topArtists = statsRepoPort.findTopArtists(
                range.getFromDateTime(), range.getToDateTimeExclusive(), resolvedLimit);
        if (topArtists.isEmpty()) {
            return Collections.emptyList();
        }

        List<UUID> artistIds = topArtists.stream().map(ArtistPlayCount::getArtistId).toList();
        Map<UUID, Artist> artistById = artistRepoPort.findByIds(artistIds).stream()
                .collect(Collectors.toMap(Artist::getId, artist -> artist));

        return topArtists.stream()
                .map(item -> {
                    Artist artist = artistById.get(item.getArtistId());
                    if (artist == null) {
                        return null;
                    }
                    return AdminTopArtistResponse.builder()
                            .id(artist.getId())
                            .name(artist.getName())
                            .avatarUrl(artist.getAvatarUrl())
                            .playCount(item.getPlayCount())
                            .build();
                })
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminDailyPlayResponse> getDailyPlays(LocalDate from, LocalDate to) {
        DateRange range = resolveRange(from, to, DEFAULT_RANGE_DAYS);

        List<DailyPlayCount> counts = statsRepoPort.findDailyPlays(
                range.getFromDateTime(), range.getToDateTimeExclusive());

        Map<LocalDate, Long> countByDate = counts.stream()
                .collect(Collectors.toMap(DailyPlayCount::getDate, DailyPlayCount::getPlayCount));

        Map<LocalDate, Long> ordered = new LinkedHashMap<>();
        LocalDate cursor = range.getFromDate();
        while (!cursor.isAfter(range.getToDate())) {
            ordered.put(cursor, countByDate.getOrDefault(cursor, 0L));
            cursor = cursor.plusDays(1);
        }

        return ordered.entrySet().stream()
                .map(entry -> new AdminDailyPlayResponse(entry.getKey(), entry.getValue()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AdminDauMauResponse getDauMau(LocalDate date) {
        LocalDate targetDate = date != null ? date : LocalDate.now();
        LocalDateTime dauStart = targetDate.atStartOfDay();
        LocalDateTime dauEnd = targetDate.plusDays(1).atStartOfDay();

        LocalDate mauStartDate = targetDate.minusDays(DEFAULT_RANGE_DAYS - 1L);
        LocalDateTime mauStart = mauStartDate.atStartOfDay();

        long dau = statsRepoPort.countDistinctUsers(dauStart, dauEnd);
        long mau = statsRepoPort.countDistinctUsers(mauStart, dauEnd);

        return AdminDauMauResponse.builder()
                .date(targetDate)
                .dau(dau)
                .mau(mau)
                .mauWindowDays(DEFAULT_RANGE_DAYS)
                .build();
    }

    private int resolveLimit(Integer limit) {
        if (limit == null || limit < 1) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private DateRange resolveRange(LocalDate from, LocalDate to, int defaultDays) {
        LocalDate resolvedTo = (to != null) ? to : LocalDate.now();
        LocalDate resolvedFrom = (from != null) ? from : resolvedTo.minusDays(defaultDays - 1L);
        if (resolvedFrom.isAfter(resolvedTo)) {
            LocalDate temp = resolvedFrom;
            resolvedFrom = resolvedTo;
            resolvedTo = temp;
        }
        return new DateRange(resolvedFrom, resolvedTo);
    }

    private static class DateRange {

        private final LocalDate fromDate;
        private final LocalDate toDate;

        private DateRange(LocalDate fromDate, LocalDate toDate) {
            this.fromDate = fromDate;
            this.toDate = toDate;
        }

        private LocalDate getFromDate() {
            return fromDate;
        }

        private LocalDate getToDate() {
            return toDate;
        }

        private LocalDateTime getFromDateTime() {
            return fromDate.atStartOfDay();
        }

        private LocalDateTime getToDateTimeExclusive() {
            return toDate.plusDays(1).atStartOfDay();
        }
    }
}
