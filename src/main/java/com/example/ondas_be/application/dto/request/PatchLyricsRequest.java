package com.example.ondas_be.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * Request DTO for partially updating an existing lyrics record.
 * Only non-null fields will be patched.
 * - Set {@code syncedLines} to an empty list to delete synced lines only.
 * - Set {@code syncedLines} to a valid list to replace all synced lines.
 */
@Data
public class PatchLyricsRequest {

    /** Language code, e.g. "en", "vi", "ja". */
    @Size(max = 10, message = "Language code must not exceed 10 characters")
    private String language;

    /** Plain text lyrics (non-timed). Null means no change. */
    private String plainText;

    /**
     * Synced lyric lines. Null means no change to synced lines.
     * Empty list means delete synced lines (keep plain text).
     * Non-empty list means replace all synced lines.
     */
    @Valid
    private List<SyncedLyricsLineDto> syncedLines;
}
