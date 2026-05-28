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
    @NotNull(message = "validation.not_null")
    @PositiveOrZero(message = "validation.positive_or_zero")
    private Integer startMs;

    /** End time in milliseconds (optional, must be > startMs if provided). */
    private Integer endMs;

    /** The text content of this line. */
    @NotBlank(message = "validation.not_blank")
    private String lineText;

    /** 0-based ordering index. */
    @NotNull(message = "validation.not_null")
    private Short lineIndex;
}
