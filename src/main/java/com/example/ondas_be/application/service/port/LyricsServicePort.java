package com.example.ondas_be.application.service.port;

import com.example.ondas_be.application.dto.request.UpdateStaticLyricsRequest;
import com.example.ondas_be.application.dto.response.LyricsResponse;

import java.util.UUID;

public interface LyricsServicePort {
    LyricsResponse getLyricsBySongId(UUID songId);
    LyricsResponse updateStaticLyrics(UUID songId, UpdateStaticLyricsRequest request);
    void deleteLyrics(UUID songId);
}
