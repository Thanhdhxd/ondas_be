package com.example.ondas_be.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Domain entity representing lyrics for a song.
 * A song has exactly one {@code Lyrics} record, which may contain
 * plain text lyrics and/or synced (karaoke-style) lyrics.
 */
@Getter
@AllArgsConstructor
@Builder
public class Lyrics {

    private UUID id;
    private UUID songId;
    private String plainText;
    private boolean hasSynced;
    private String language;
    private UUID createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** Loaded on demand — not always populated. */
    private List<SyncedLyricsLine> syncedLines;
}
