package com.example.ondas_be.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

/**
 * A single synced lyric line within a {@link SaveSyncedLyricsRequest}.
 */
@Data
public class SyncedLyricsLineDto {

    /** Start time in milliseconds (≥ 0). */
    @NotNull(message = "startMs is required")
    @PositiveOrZero(message = "startMs must be ≥ 0")
    private Integer startMs;

    /** End time in milliseconds (optional, must be > startMs if provided). */
    private Integer endMs;

    /** The text content of this line. */
    @NotBlank(message = "lineText is required")
    private String lineText;

    /** 0-based ordering index. */
    @NotNull(message = "lineIndex is required")
    private Short lineIndex;
}
