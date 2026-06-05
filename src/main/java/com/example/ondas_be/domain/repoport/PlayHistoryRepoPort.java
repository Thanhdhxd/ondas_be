package com.example.ondas_be.domain.repoport;

import com.example.ondas_be.domain.entity.ArtistPlayCount;
import com.example.ondas_be.domain.entity.PlayHistory;
import com.example.ondas_be.domain.entity.SongPlayCount;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlayHistoryRepoPort {

    PlayHistory save(PlayHistory playHistory);

    List<PlayHistory> findByUserId(UUID userId, int page, int size);

    long countByUserId(UUID userId);

    Optional<PlayHistory> findByIdAndUserId(Long id, UUID userId);

    void deleteAllByUserId(UUID userId);

    void deleteByIdAndUserId(Long id, UUID userId);

    long sumListeningDurationByUserId(UUID userId);

    List<SongPlayCount> findTopSongsByUserId(UUID userId, int limit);

    List<ArtistPlayCount> findTopArtistsByUserId(UUID userId, int limit);
}
