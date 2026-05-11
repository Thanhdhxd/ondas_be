package com.example.ondas_be.domain.repoport;

import com.example.ondas_be.domain.entity.SyncedLyricsLine;

import java.util.List;
import java.util.UUID;

/**
 * Repository contract for {@link SyncedLyricsLine} persistence.
 */
public interface SyncedLyricsLineRepoPort {

    /** Replace ALL lines for a given lyrics record in a single transaction. */
    void replaceLines(UUID lyricsId, List<SyncedLyricsLine> lines);

    /** Retrieve all lines for a lyrics record, ordered by {@code lineIndex}. */
    List<SyncedLyricsLine> findByLyricsId(UUID lyricsId);

    /** Delete all synced lines for a lyrics record. */
    void deleteByLyricsId(UUID lyricsId);
}
