package com.example.ondas_be.infrastructure.persistence.adapter;

import com.example.ondas_be.domain.entity.Lyrics;
import com.example.ondas_be.domain.repoport.LyricsRepoPort;
import com.example.ondas_be.infrastructure.persistence.jparepo.LyricsJpaRepo;
import com.example.ondas_be.infrastructure.persistence.model.LyricsModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LyricsAdapter implements LyricsRepoPort {

    private final LyricsJpaRepo lyricsJpaRepo;

    @Override
    public Optional<Lyrics> findBySongId(UUID songId) {
        return lyricsJpaRepo.findBySongId(songId).map(LyricsModel::toDomain);
    }

    @Override
    public Lyrics save(Lyrics lyrics) {
        LyricsModel model = LyricsModel.fromDomain(lyrics);
        // Bảo toàn các trường auditing nếu cần
        if (lyrics.getCreatedAt() != null) {
            model.setCreatedAt(lyrics.getCreatedAt());
        }
        if (lyrics.getUpdatedAt() != null) {
            model.setUpdatedAt(lyrics.getUpdatedAt());
        }
        return lyricsJpaRepo.save(model).toDomain();
    }

    @Override
    public void deleteBySongId(UUID songId) {
        lyricsJpaRepo.deleteBySongId(songId);
    }
}
