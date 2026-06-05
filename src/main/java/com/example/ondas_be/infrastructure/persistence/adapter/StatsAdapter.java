package com.example.ondas_be.infrastructure.persistence.adapter;

import com.example.ondas_be.domain.entity.ArtistPlayCount;
import com.example.ondas_be.domain.entity.DailyPlayCount;
import com.example.ondas_be.domain.entity.SongPlayCount;
import com.example.ondas_be.domain.repoport.StatsRepoPort;
import com.example.ondas_be.infrastructure.persistence.jparepo.PlayHistoryJpaRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class StatsAdapter implements StatsRepoPort {

    private final PlayHistoryJpaRepo playHistoryJpaRepo;

    @Override
    public List<SongPlayCount> findTopSongs(LocalDateTime from, LocalDateTime toExclusive, int limit) {
        return playHistoryJpaRepo.findTopSongs(from, toExclusive, limit).stream()
                .map(row -> new SongPlayCount(row.getSongId(), nullSafeCount(row.getPlayCount())))
                .toList();
    }

    @Override
    public List<ArtistPlayCount> findTopArtists(LocalDateTime from, LocalDateTime toExclusive, int limit) {
        return playHistoryJpaRepo.findTopArtists(from, toExclusive, limit).stream()
                .map(row -> new ArtistPlayCount(row.getArtistId(), nullSafeCount(row.getPlayCount())))
                .toList();
    }

    @Override
    public List<DailyPlayCount> findDailyPlays(LocalDateTime from, LocalDateTime toExclusive) {
        return playHistoryJpaRepo.findDailyPlays(from, toExclusive).stream()
                .map(row -> new DailyPlayCount(row.getDay(), nullSafeCount(row.getPlayCount())))
                .toList();
    }

    @Override
    public long countDistinctUsers(LocalDateTime from, LocalDateTime toExclusive) {
        return playHistoryJpaRepo.countDistinctUsers(from, toExclusive);
    }

    private long nullSafeCount(Long value) {
        return value == null ? 0L : value;
    }
}
