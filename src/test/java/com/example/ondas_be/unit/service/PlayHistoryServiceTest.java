package com.example.ondas_be.unit.service;

import com.example.ondas_be.application.dto.common.PageResultDto;
import com.example.ondas_be.application.dto.response.PlayHistoryResponse;
import com.example.ondas_be.application.exception.PlayHistoryNotFoundException;
import com.example.ondas_be.application.exception.SongNotFoundException;
import com.example.ondas_be.application.service.impl.PlayHistoryService;
import com.example.ondas_be.domain.entity.PlayHistory;
import com.example.ondas_be.domain.entity.Role;
import com.example.ondas_be.domain.entity.Song;
import com.example.ondas_be.domain.entity.User;
import com.example.ondas_be.domain.repoport.PlayHistoryRepoPort;
import com.example.ondas_be.domain.repoport.SongRepoPort;
import com.example.ondas_be.domain.repoport.UserRepoPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlayHistoryServiceTest {

    @Mock
    private PlayHistoryRepoPort playHistoryRepoPort;

    @Mock
    private SongRepoPort songRepoPort;

    @Mock
    private UserRepoPort userRepoPort;

    @InjectMocks
    private PlayHistoryService playHistoryService;

    private static final String EMAIL = "user@example.com";
    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID SONG_ID = UUID.randomUUID();

    private User buildUser() {
        return new User(USER_ID, EMAIL, null, "Test User", null,
                true, null, null, null, Role.USER,
                LocalDateTime.now(), LocalDateTime.now());
    }

    private Song buildSong() {
        return new Song(SONG_ID, "Test Song", "test-song", 210, "http://audio.url",
                "mp3", null, null, null, null, null,
                0L, true, null, LocalDateTime.now(), LocalDateTime.now(), null, null);
    }

    private PlayHistory buildSavedHistory(Long id) {
        return new PlayHistory(id, USER_ID, SONG_ID, LocalDateTime.now(), "home");
    }

    // ── getMyHistory ────────────────────────────────────────────────────────────

    @Test
    void getMyHistory_WhenValid_ShouldReturnPagedResult() {
        PlayHistory entry = buildSavedHistory(1L);

        when(userRepoPort.findByEmail(EMAIL)).thenReturn(Optional.of(buildUser()));
        when(playHistoryRepoPort.findByUserId(USER_ID, 0, 20)).thenReturn(List.of(entry));
        when(playHistoryRepoPort.countByUserId(USER_ID)).thenReturn(1L);
        when(songRepoPort.findByIds(List.of(SONG_ID))).thenReturn(List.of(buildSong()));

        PageResultDto<PlayHistoryResponse> result = playHistoryService.getMyHistory(EMAIL, 0, 20);

        assertNotNull(result);
        assertEquals(1, result.getItems().size());
        assertEquals(1L, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertEquals(SONG_ID, result.getItems().get(0).getSong().getId());
    }

    // ── clearMyHistory ──────────────────────────────────────────────────────────

    @Test
    void clearMyHistory_ShouldCallDeleteAllByUserId() {
        when(userRepoPort.findByEmail(EMAIL)).thenReturn(Optional.of(buildUser()));

        playHistoryService.clearMyHistory(EMAIL);

        verify(playHistoryRepoPort).deleteAllByUserId(USER_ID);
    }

    // ── deleteHistoryEntry ──────────────────────────────────────────────────────

    @Test
    void deleteHistoryEntry_WhenEntryExists_ShouldDelete() {
        PlayHistory entry = buildSavedHistory(42L);

        when(userRepoPort.findByEmail(EMAIL)).thenReturn(Optional.of(buildUser()));
        when(playHistoryRepoPort.findByIdAndUserId(42L, USER_ID)).thenReturn(Optional.of(entry));

        playHistoryService.deleteHistoryEntry(EMAIL, 42L);

        verify(playHistoryRepoPort).deleteByIdAndUserId(42L, USER_ID);
    }

    @Test
    void deleteHistoryEntry_WhenEntryNotFound_ShouldThrowPlayHistoryNotFoundException() {
        when(userRepoPort.findByEmail(EMAIL)).thenReturn(Optional.of(buildUser()));
        when(playHistoryRepoPort.findByIdAndUserId(99L, USER_ID)).thenReturn(Optional.empty());

        assertThrows(PlayHistoryNotFoundException.class,
                () -> playHistoryService.deleteHistoryEntry(EMAIL, 99L));
        verify(playHistoryRepoPort, never()).deleteByIdAndUserId(any(), any());
    }

    // ── recordPlay ──────────────────────────────────────────────────────────────

    @Test
    void recordPlay_WhenValid_ShouldSaveHistoryAndIncrementPlayCount() {
        when(userRepoPort.findByEmail(EMAIL)).thenReturn(Optional.of(buildUser()));
        when(songRepoPort.findById(SONG_ID)).thenReturn(Optional.of(buildSong()));

        playHistoryService.recordPlay(SONG_ID, EMAIL, "home");

        verify(playHistoryRepoPort).save(any(PlayHistory.class));
        verify(songRepoPort).incrementPlayCount(SONG_ID);
    }

    @Test
    void recordPlay_WhenSongNotFound_ShouldThrowSongNotFoundException() {
        when(userRepoPort.findByEmail(EMAIL)).thenReturn(Optional.of(buildUser()));
        when(songRepoPort.findById(SONG_ID)).thenReturn(Optional.empty());

        assertThrows(SongNotFoundException.class,
                () -> playHistoryService.recordPlay(SONG_ID, EMAIL, "home"));
        verify(playHistoryRepoPort, never()).save(any());
        verify(songRepoPort, never()).incrementPlayCount(any());
    }

    @Test
    void recordPlay_WhenSongInactive_ShouldThrowSongNotFoundException() {
        Song inactiveSong = new Song(SONG_ID, "Inactive", "inactive", 210, "http://audio.url",
                "mp3", null, null, null, null, null,
                0L, false, null, LocalDateTime.now(), LocalDateTime.now(), null, null);
        when(userRepoPort.findByEmail(EMAIL)).thenReturn(Optional.of(buildUser()));
        when(songRepoPort.findById(SONG_ID)).thenReturn(Optional.of(inactiveSong));

        assertThrows(SongNotFoundException.class,
                () -> playHistoryService.recordPlay(SONG_ID, EMAIL, "home"));
        verify(playHistoryRepoPort, never()).save(any());
    }
}
