package com.example.ondas_be.infrastructure.persistence.adapter;

import com.example.ondas_be.domain.entity.Lyrics;
import com.example.ondas_be.domain.repoport.LyricsRepoPort;
import com.example.ondas_be.infrastructure.persistence.jparepo.LyricsJpaRepo;
import com.example.ondas_be.infrastructure.persistence.model.LyricsModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LyricsAdapter implements LyricsRepoPort {

    private final LyricsJpaRepo lyricsJpaRepo;

    @Override
    public Lyrics save(Lyrics lyrics) {
        return lyricsJpaRepo.save(LyricsModel.fromDomain(lyrics)).toDomain();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Lyrics> findById(UUID id) {
        return lyricsJpaRepo.findById(id).map(LyricsModel::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Lyrics> findBySongId(UUID songId) {
        return lyricsJpaRepo.findBySongId(songId).map(LyricsModel::toDomain);
    }

    @Override
    @Transactional
    public void updateStaticLyrics(UUID id, String plainText, String language) {
        lyricsJpaRepo.updateStaticLyrics(id, plainText, language);
    }

    @Override
    @Transactional
    public void updateHasSynced(UUID id, boolean hasSynced) {
        lyricsJpaRepo.updateHasSynced(id, hasSynced);
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        lyricsJpaRepo.deleteById(id);
    }
}
