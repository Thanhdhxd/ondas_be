package com.example.ondas_be.unit.service;

import com.example.ondas_be.application.dto.request.UpdateStaticLyricsRequest;
import com.example.ondas_be.application.dto.response.LyricsResponse;
import com.example.ondas_be.application.exception.LyricsNotFoundException;
import com.example.ondas_be.application.exception.SongNotFoundException;
import com.example.ondas_be.application.service.impl.LyricsService;
import com.example.ondas_be.domain.entity.Lyrics;
import com.example.ondas_be.domain.entity.Song;
import com.example.ondas_be.domain.repoport.LyricsRepoPort;
import com.example.ondas_be.domain.repoport.SongRepoPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LyricsServiceTest {

    @Mock
    private LyricsRepoPort lyricsRepoPort;

    @Mock
    private SongRepoPort songRepoPort;

    @InjectMocks
    private LyricsService lyricsService;

    private UUID songId;
    private UUID lyricsId;
    private Lyrics existingLyrics;

    @BeforeEach
    void setUp() {
        songId = UUID.randomUUID();
        lyricsId = UUID.randomUUID();
        
        existingLyrics = new Lyrics(
                lyricsId,
                songId,
                "Original lyrics",
                false,
                LocalDateTime.now(),
                LocalDateTime.now(),
                null
        );
    }

    // --- getLyricsBySongId ---
    
    @Test
    void getLyricsBySongId_ShouldReturnResponse_WhenLyricsExist() {
        when(lyricsRepoPort.findBySongId(songId)).thenReturn(Optional.of(existingLyrics));

        LyricsResponse response = lyricsService.getLyricsBySongId(songId);

        assertNotNull(response);
        assertEquals(songId, response.getSongId());
        assertEquals("Original lyrics", response.getPlainText());
        assertFalse(response.isHasSynced());
        
        verify(lyricsRepoPort).findBySongId(songId);
    }

    @Test
    void getLyricsBySongId_ShouldThrowLyricsNotFoundException_WhenNotFound() {
        when(lyricsRepoPort.findBySongId(songId)).thenReturn(Optional.empty());

        assertThrows(LyricsNotFoundException.class, () -> lyricsService.getLyricsBySongId(songId));
        verify(lyricsRepoPort).findBySongId(songId);
    }

    // --- updateStaticLyrics ---

    @Test
    void updateStaticLyrics_ShouldCreateNewLyrics_WhenNotExist() {
        UpdateStaticLyricsRequest request = new UpdateStaticLyricsRequest();
        request.setPlainText("New Lyrics");

        Song dummySong = mock(Song.class); // Giả lập song tồn tại
        when(songRepoPort.findById(songId)).thenReturn(Optional.of(dummySong));
        when(lyricsRepoPort.findBySongId(songId)).thenReturn(Optional.empty());

        when(lyricsRepoPort.save(any(Lyrics.class))).thenAnswer(inv -> {
            Lyrics l = inv.getArgument(0);
            return new Lyrics(UUID.randomUUID(), l.getSongId(), l.getPlainText(), l.isHasSynced(), l.getCreatedAt(), l.getUpdatedAt(), l.getCreatedBy());
        });

        LyricsResponse response = lyricsService.updateStaticLyrics(songId, request);

        assertNotNull(response);
        assertEquals("New Lyrics", response.getPlainText());
        verify(lyricsRepoPort).save(argThat(l -> l.getId() == null && "New Lyrics".equals(l.getPlainText())));
    }

    @Test
    void updateStaticLyrics_ShouldUpdateExistingLyrics_WhenExist() {
        UpdateStaticLyricsRequest request = new UpdateStaticLyricsRequest();
        request.setPlainText("Updated Lyrics");

        Song dummySong = mock(Song.class);
        when(songRepoPort.findById(songId)).thenReturn(Optional.of(dummySong));
        when(lyricsRepoPort.findBySongId(songId)).thenReturn(Optional.of(existingLyrics));

        when(lyricsRepoPort.save(any(Lyrics.class))).thenAnswer(inv -> inv.getArgument(0));

        LyricsResponse response = lyricsService.updateStaticLyrics(songId, request);

        assertNotNull(response);
        assertEquals("Updated Lyrics", response.getPlainText());
        assertEquals(lyricsId, response.getId());
        verify(lyricsRepoPort).save(argThat(l -> l.getId().equals(lyricsId) && "Updated Lyrics".equals(l.getPlainText())));
    }

    @Test
    void updateStaticLyrics_ShouldThrowSongNotFoundException_WhenSongDoesNotExist() {
        UpdateStaticLyricsRequest request = new UpdateStaticLyricsRequest();
        request.setPlainText("Text");

        when(songRepoPort.findById(songId)).thenReturn(Optional.empty());

        assertThrows(SongNotFoundException.class, () -> lyricsService.updateStaticLyrics(songId, request));
        verify(lyricsRepoPort, never()).save(any());
    }

    // --- deleteLyrics ---

    @Test
    void deleteLyrics_ShouldDelete_WhenLyricsExist() {
        when(lyricsRepoPort.findBySongId(songId)).thenReturn(Optional.of(existingLyrics));

        lyricsService.deleteLyrics(songId);

        verify(lyricsRepoPort).deleteBySongId(songId);
    }

    @Test
    void deleteLyrics_ShouldThrowLyricsNotFoundException_WhenLyricsNotExist() {
        when(lyricsRepoPort.findBySongId(songId)).thenReturn(Optional.empty());

        assertThrows(LyricsNotFoundException.class, () -> lyricsService.deleteLyrics(songId));
        verify(lyricsRepoPort, never()).deleteBySongId(any());
    }
}
