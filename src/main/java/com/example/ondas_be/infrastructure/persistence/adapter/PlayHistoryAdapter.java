package com.example.ondas_be.infrastructure.persistence.adapter;

import com.example.ondas_be.domain.entity.ArtistPlayCount;
import com.example.ondas_be.domain.entity.PlayHistory;
import com.example.ondas_be.domain.entity.SongPlayCount;
import com.example.ondas_be.domain.repoport.PlayHistoryRepoPort;
import com.example.ondas_be.infrastructure.persistence.jparepo.PlayHistoryJpaRepo;
import com.example.ondas_be.infrastructure.persistence.model.PlayHistoryModel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PlayHistoryAdapter implements PlayHistoryRepoPort {

    private final PlayHistoryJpaRepo playHistoryJpaRepo;

    @Override
    public PlayHistory save(PlayHistory playHistory) {
        return playHistoryJpaRepo.save(PlayHistoryModel.fromDomain(playHistory)).toDomain();
    }

    @Override
    public List<PlayHistory> findByUserId(UUID userId, int page, int size) {
        return playHistoryJpaRepo.findByUserIdOrderByPlayedAtDesc(userId, PageRequest.of(page, size))
                .map(PlayHistoryModel::toDomain)
                .toList();
    }

    @Override
    public long countByUserId(UUID userId) {
        return playHistoryJpaRepo.countByUserId(userId);
    }

    @Override
    public Optional<PlayHistory> findByIdAndUserId(Long id, UUID userId) {
        return playHistoryJpaRepo.findByIdAndUserId(id, userId).map(PlayHistoryModel::toDomain);
    }

    @Override
    @Transactional
    public void deleteAllByUserId(UUID userId) {
        playHistoryJpaRepo.deleteAllByUserId(userId);
    }

    @Override
    @Transactional
    public void deleteByIdAndUserId(Long id, UUID userId) {
        playHistoryJpaRepo.deleteByIdAndUserId(id, userId);
    }

    @Override
    public long sumListeningDurationByUserId(UUID userId) {
        return playHistoryJpaRepo.sumListeningDurationByUserId(userId);
    }

    @Override
    public List<SongPlayCount> findTopSongsByUserId(UUID userId, int limit) {
        return playHistoryJpaRepo.findMyTopSongs(userId, limit).stream()
                .map(row -> new SongPlayCount(row.getSongId(), row.getPlayCount() == null ? 0L : row.getPlayCount()))
                .toList();
    }

    @Override
    public List<ArtistPlayCount> findTopArtistsByUserId(UUID userId, int limit) {
        return playHistoryJpaRepo.findMyTopArtists(userId, limit).stream()
                .map(row -> new ArtistPlayCount(row.getArtistId(), row.getPlayCount() == null ? 0L : row.getPlayCount()))
                .toList();
    }
}
