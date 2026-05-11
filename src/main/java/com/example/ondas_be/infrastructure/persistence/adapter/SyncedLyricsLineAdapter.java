package com.example.ondas_be.infrastructure.persistence.adapter;

import com.example.ondas_be.domain.entity.SyncedLyricsLine;
import com.example.ondas_be.domain.repoport.SyncedLyricsLineRepoPort;
import com.example.ondas_be.infrastructure.persistence.jparepo.SyncedLyricsLineJpaRepo;
import com.example.ondas_be.infrastructure.persistence.model.SyncedLyricsLineModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SyncedLyricsLineAdapter implements SyncedLyricsLineRepoPort {

    private final SyncedLyricsLineJpaRepo syncedLyricsLineJpaRepo;

    @Override
    @Transactional
    public void replaceLines(UUID lyricsId, List<SyncedLyricsLine> lines) {
        syncedLyricsLineJpaRepo.deleteByLyricsId(lyricsId);
        List<SyncedLyricsLineModel> models = lines.stream()
                .map(SyncedLyricsLineModel::fromDomain)
                .toList();
        syncedLyricsLineJpaRepo.saveAll(models);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SyncedLyricsLine> findByLyricsId(UUID lyricsId) {
        return syncedLyricsLineJpaRepo.findByLyricsIdOrderByLineIndexAsc(lyricsId)
                .stream()
                .map(SyncedLyricsLineModel::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public void deleteByLyricsId(UUID lyricsId) {
        syncedLyricsLineJpaRepo.deleteByLyricsId(lyricsId);
    }
}
