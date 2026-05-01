package com.example.ondas_be.presentation.controller;

import com.example.ondas_be.application.dto.common.ApiResponse;
import com.example.ondas_be.application.dto.request.UpdateStaticLyricsRequest;
import com.example.ondas_be.application.dto.response.LyricsResponse;
import com.example.ondas_be.application.service.port.LyricsServicePort;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/songs/{songId}/lyrics")
@RequiredArgsConstructor
public class LyricsController {

    private final LyricsServicePort lyricsServicePort;

    @GetMapping
    public ResponseEntity<ApiResponse<LyricsResponse>> getLyrics(@PathVariable UUID songId) {
        return ResponseEntity.ok(ApiResponse.success(lyricsServicePort.getLyricsBySongId(songId)));
    }

    @PutMapping("/static")
    @PreAuthorize("hasAnyRole('ADMIN','CONTENT_MANAGER')")
    public ResponseEntity<ApiResponse<LyricsResponse>> updateStaticLyrics(
            @PathVariable UUID songId,
            @Valid @RequestBody UpdateStaticLyricsRequest request) {
        return ResponseEntity.ok(ApiResponse.success(lyricsServicePort.updateStaticLyrics(songId, request)));
    }

    @DeleteMapping
    @PreAuthorize("hasAnyRole('ADMIN','CONTENT_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deleteLyrics(@PathVariable UUID songId) {
        lyricsServicePort.deleteLyrics(songId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
