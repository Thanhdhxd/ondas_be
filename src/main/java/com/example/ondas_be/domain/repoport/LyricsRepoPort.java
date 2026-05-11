package com.example.ondas_be.domain.repoport;

import com.example.ondas_be.domain.entity.Lyrics;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository contract for {@link Lyrics} persistence.
 */
public interface LyricsRepoPort {

    Lyrics save(Lyrics lyrics);

    Optional<Lyrics> findById(UUID id);

    /** Find lyrics by song ID (1-to-1). */
    Optional<Lyrics> findBySongId(UUID songId);

    /** Update only the plain_text and language columns. */
    void updateStaticLyrics(UUID id, String plainText, String language);

    /** Set has_synced flag. */
    void updateHasSynced(UUID id, boolean hasSynced);

    void deleteById(UUID id);
}
