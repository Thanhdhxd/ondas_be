package com.example.ondas_be.application.service.impl;

import com.example.ondas_be.application.dto.request.UpdateStaticLyricsRequest;
import com.example.ondas_be.application.dto.response.LyricsResponse;
import com.example.ondas_be.application.exception.LyricsNotFoundException;
import com.example.ondas_be.application.exception.SongNotFoundException;
import com.example.ondas_be.application.service.port.LyricsServicePort;
import com.example.ondas_be.domain.entity.Lyrics;
import com.example.ondas_be.domain.repoport.LyricsRepoPort;
import com.example.ondas_be.domain.repoport.SongRepoPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LyricsService implements LyricsServicePort {

    private final LyricsRepoPort lyricsRepoPort;
    private final SongRepoPort songRepoPort;

    @Override
    @Transactional(readOnly = true)
    public LyricsResponse getLyricsBySongId(UUID songId) {
        Lyrics lyrics = lyricsRepoPort.findBySongId(songId)
                .orElseThrow(() -> new LyricsNotFoundException("Lyrics not found for song: " + songId));
        return toResponse(lyrics);
    }

    @Override
    @Transactional
    public LyricsResponse updateStaticLyrics(UUID songId, UpdateStaticLyricsRequest request) {
        boolean songExists = songRepoPort.findById(songId).isPresent();
        if (!songExists) {
            throw new SongNotFoundException("Song not found with id: " + songId);
        }

        Optional<Lyrics> existingOpt = lyricsRepoPort.findBySongId(songId);
        Lyrics lyricsToSave;

        if (existingOpt.isPresent()) {
            Lyrics existing = existingOpt.get();
            lyricsToSave = new Lyrics(
                    existing.getId(),
                    existing.getSongId(),
                    request.getPlainText(),
                    existing.isHasSynced(),
                    existing.getCreatedAt(),
                    LocalDateTime.now(),
                    existing.getCreatedBy()
            );
        } else {
            lyricsToSave = new Lyrics(
                    null,
                    songId,
                    request.getPlainText(),
                    false,
                    LocalDateTime.now(),
                    LocalDateTime.now(),
                    null
            );
        }

        Lyrics saved = lyricsRepoPort.save(lyricsToSave);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteLyrics(UUID songId) {
        lyricsRepoPort.findBySongId(songId)
                .orElseThrow(() -> new LyricsNotFoundException("Lyrics not found for song: " + songId));
        lyricsRepoPort.deleteBySongId(songId);
    }

    private LyricsResponse toResponse(Lyrics lyrics) {
        return LyricsResponse.builder()
                .id(lyrics.getId())
                .songId(lyrics.getSongId())
                .plainText(lyrics.getPlainText())
                .hasSynced(lyrics.isHasSynced())
                .build();
    }
}
