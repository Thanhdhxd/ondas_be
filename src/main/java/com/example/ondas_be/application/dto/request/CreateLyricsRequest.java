package com.example.ondas_be.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * Request DTO for creating new lyrics (plain text + optional synced lines)
 * for a song that currently has no lyrics.
 */
@Data
public class CreateLyricsRequest {

    /** Language code, e.g. "en", "vi", "ja". */
    @Size(max = 10, message = "Language code must not exceed 10 characters")
    private String language;

    /** Plain text lyrics (non-timed). */
    private String plainText;

    /** Optional synced (karaoke-style) lyric lines. */
    @Valid
    private List<SyncedLyricsLineDto> syncedLines;
}
