package com.example.ondas_be.application.service.port;

import com.example.ondas_be.application.dto.request.CreateLyricsRequest;
import com.example.ondas_be.application.dto.request.PatchLyricsRequest;
import com.example.ondas_be.application.dto.response.LyricsResponse;

import java.util.UUID;

/**
 * Service contract for lyrics operations.
 *
 * <h3>RESTful operation mapping</h3>
 * <ul>
 *   <li>{@code GET}    → {@link #getLyricsBySongId(UUID)}</li>
 *   <li>{@code POST}   → {@link #createLyrics(UUID, CreateLyricsRequest)}</li>
 *   <li>{@code PATCH}  → {@link #patchLyrics(UUID, PatchLyricsRequest)}</li>
 *   <li>{@code DELETE} → {@link #deleteLyrics(UUID)}</li>
 * </ul>
 */
public interface LyricsServicePort {

    /**
     * Get full lyrics (plain text + synced lines if available) for a song.
     * Returns 404 if no lyrics exist for this song.
     */
    LyricsResponse getLyricsBySongId(UUID songId);

    /**
     * Create a new lyrics record for a song.
     * Fails with 409 if lyrics already exist for this song.
     */
    LyricsResponse createLyrics(UUID songId, CreateLyricsRequest request);

    /**
     * Partially update an existing lyrics record.
     * Only non-null fields in the request are applied.
     * <ul>
     *   <li>{@code syncedLines = null} → don't touch synced lines</li>
     *   <li>{@code syncedLines = []}   → delete synced lines only</li>
     *   <li>{@code syncedLines = [...]}→ replace all synced lines</li>
     * </ul>
     * Fails with 404 if lyrics don't exist for this song.
     */
    LyricsResponse patchLyrics(UUID songId, PatchLyricsRequest request);

    /**
     * Delete the entire lyrics record (plain text + synced lines) for a song.
     */
    void deleteLyrics(UUID songId);
}
