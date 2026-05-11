package com.example.ondas_be.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Domain entity representing a single line in synced/timed lyrics.
 * Belongs to a parent {@code Lyrics} record.
 */
@Getter
@AllArgsConstructor
public class SyncedLyricsLine {

    /** Database-generated ID (SERIAL). Null for new lines. */
    private Integer id;

    /** FK to {@code lyrics.id}. */
    private java.util.UUID lyricsId;

    /** Start time of this line in milliseconds. */
    private Integer startMs;

    /** End time of this line in milliseconds. Nullable (last line or unknown). */
    private Integer endMs;

    /** The text content of this line. */
    private String lineText;

    /** 0-based line index for ordering. */
    private Short lineIndex;
}
