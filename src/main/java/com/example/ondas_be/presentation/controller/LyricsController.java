package com.example.ondas_be.presentation.controller;

import com.example.ondas_be.application.dto.common.ApiResponse;
import com.example.ondas_be.application.dto.request.CreateLyricsRequest;
import com.example.ondas_be.application.dto.request.PatchLyricsRequest;
import com.example.ondas_be.application.dto.response.LyricsResponse;
import com.example.ondas_be.application.service.port.LyricsServicePort;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST controller for lyrics of a song.
 *
 * <h3>Endpoints</h3>
 * <pre>
 * GET    /api/songs/{songId}/lyrics  — Lấy toàn bộ lyrics (plain + synced)
 * POST   /api/songs/{songId}/lyrics  — Tạo mới lyrics (409 nếu đã tồn tại)
 * PATCH  /api/songs/{songId}/lyrics  — Cập nhật một phần lyrics
 * DELETE /api/songs/{songId}/lyrics  — Xoá toàn bộ lyrics
 * </pre>
 */
@RestController
@RequestMapping("/api/songs/{songId}/lyrics")
@RequiredArgsConstructor
public class LyricsController {

    private final LyricsServicePort lyricsServicePort;

    /** Lấy toàn bộ lyrics (plain text + synced nếu có) của một bài hát. */
    @GetMapping
    public ResponseEntity<ApiResponse<LyricsResponse>> getLyrics(@PathVariable UUID songId) {
        return ResponseEntity.ok(ApiResponse.success(lyricsServicePort.getLyricsBySongId(songId)));
    }

    /** Tạo mới lyrics cho bài hát. Trả về 409 nếu lyrics đã tồn tại. */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','CONTENT_MANAGER')")
    public ResponseEntity<ApiResponse<LyricsResponse>> createLyrics(
            @PathVariable UUID songId,
            @Valid @RequestBody CreateLyricsRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(lyricsServicePort.createLyrics(songId, request)));
    }

    /**
     * Cập nhật một phần lyrics.
     * <ul>
     *   <li>{@code syncedLines = null} → không đụng đến synced</li>
     *   <li>{@code syncedLines = []}   → xoá synced lines (giữ plain text)</li>
     *   <li>{@code syncedLines = [...]}→ thay thế toàn bộ synced lines</li>
     * </ul>
     */
    @PatchMapping
    @PreAuthorize("hasAnyRole('ADMIN','CONTENT_MANAGER')")
    public ResponseEntity<ApiResponse<LyricsResponse>> patchLyrics(
            @PathVariable UUID songId,
            @Valid @RequestBody PatchLyricsRequest request) {
        return ResponseEntity.ok(ApiResponse.success(lyricsServicePort.patchLyrics(songId, request)));
    }

    /** Xoá toàn bộ lyrics của bài hát. */
    @DeleteMapping
    @PreAuthorize("hasAnyRole('ADMIN','CONTENT_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deleteLyrics(@PathVariable UUID songId) {
        lyricsServicePort.deleteLyrics(songId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
