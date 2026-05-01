package com.example.ondas_be.domain.repoport;

import com.example.ondas_be.domain.entity.Lyrics;

import java.util.Optional;
import java.util.UUID;

public interface LyricsRepoPort {
    Optional<Lyrics> findBySongId(UUID songId);
    Lyrics save(Lyrics lyrics);
    void deleteBySongId(UUID songId);
}
