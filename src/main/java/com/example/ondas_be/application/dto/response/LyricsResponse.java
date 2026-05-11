package com.example.ondas_be.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for lyrics (plain text + optional synced lines).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LyricsResponse {

    private UUID id;
    private UUID songId;
    private String plainText;
    private boolean hasSynced;
    private String language;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** Populated only when {@code hasSynced = true} and synced lines are requested. */
    private List<SyncedLyricsLineResponse> syncedLines;
}
