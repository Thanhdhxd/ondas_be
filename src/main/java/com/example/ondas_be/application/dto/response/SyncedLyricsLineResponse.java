package com.example.ondas_be.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for a single synced lyric line.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncedLyricsLineResponse {

    private Integer id;
    private Integer startMs;
    private Integer endMs;
    private String lineText;
    private Short lineIndex;
}
