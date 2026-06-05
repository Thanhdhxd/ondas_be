package com.example.ondas_be.domain.repoport;

import com.example.ondas_be.domain.entity.ArtistPlayCount;
import com.example.ondas_be.domain.entity.DailyPlayCount;
import com.example.ondas_be.domain.entity.SongPlayCount;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsRepoPort {

    List<SongPlayCount> findTopSongs(LocalDateTime from, LocalDateTime toExclusive, int limit);

    List<ArtistPlayCount> findTopArtists(LocalDateTime from, LocalDateTime toExclusive, int limit);

    List<DailyPlayCount> findDailyPlays(LocalDateTime from, LocalDateTime toExclusive);

    long countDistinctUsers(LocalDateTime from, LocalDateTime toExclusive);
}
