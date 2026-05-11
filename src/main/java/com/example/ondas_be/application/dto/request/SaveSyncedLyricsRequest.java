package com.example.ondas_be.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * Request DTO for saving synced (karaoke-style) lyrics for a song.
 * Contains the full list of timed lyric lines.
 */
@Data
public class SaveSyncedLyricsRequest {

    /** Language code, e.g. "en", "vi", "ja". */
    @Size(max = 10, message = "Language code must not exceed 10 characters")
    private String language;

    /** The list of timed lyric lines — will replace all existing synced lines. */
    @NotEmpty(message = "Synced lines must not be empty")
    @Valid
    private List<SyncedLyricsLineDto> lines;
}
