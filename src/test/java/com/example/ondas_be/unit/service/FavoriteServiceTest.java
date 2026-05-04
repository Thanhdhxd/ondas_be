package com.example.ondas_be.unit.service;

import com.example.ondas_be.application.dto.common.PageResultDto;
import com.example.ondas_be.application.dto.response.FavoriteSongResponse;
import com.example.ondas_be.application.exception.FavoriteAlreadyExistsException;
import com.example.ondas_be.application.exception.FavoriteNotFoundException;
import com.example.ondas_be.application.exception.SongNotFoundException;
import com.example.ondas_be.application.exception.UserNotFoundException;
import com.example.ondas_be.application.mapper.ArtistMapper;
import com.example.ondas_be.application.mapper.GenreMapper;
import com.example.ondas_be.application.service.impl.FavoriteService;
import com.example.ondas_be.domain.entity.Favorite;
import com.example.ondas_be.domain.entity.Role;
import com.example.ondas_be.domain.entity.Song;
import com.example.ondas_be.domain.entity.User;
import com.example.ondas_be.domain.repoport.ArtistRepoPort;
import com.example.ondas_be.domain.repoport.FavoriteRepoPort;
import com.example.ondas_be.domain.repoport.GenreRepoPort;
import com.example.ondas_be.domain.repoport.SongArtistRepoPort;
import com.example.ondas_be.domain.repoport.SongGenreRepoPort;
import com.example.ondas_be.domain.repoport.SongRepoPort;
import com.example.ondas_be.domain.repoport.UserRepoPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceTest {

    @Mock private FavoriteRepoPort favoriteRepoPort;
    @Mock private UserRepoPort userRepoPort;
    @Mock private SongRepoPort songRepoPort;
    @Mock private SongArtistRepoPort songArtistRepoPort;
    @Mock private SongGenreRepoPort songGenreRepoPort;
    @Mock private ArtistRepoPort artistRepoPort;
    @Mock private GenreRepoPort genreRepoPort;
    @Mock private ArtistMapper artistMapper;
    @Mock private GenreMapper genreMapper;

    @InjectMocks
    private FavoriteService favoriteService;

    private static final String EMAIL = "user@example.com";
    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID SONG_ID = UUID.randomUUID();

    private User buildUser() {
        return new User(USER_ID, EMAIL, null, "Test User", null,
                true, null, null, null, Role.USER,
                LocalDateTime.now(), LocalDateTime.now());
    }

    private Song buildActiveSong() {
        return new Song(SONG_ID, "Test Song", "test-song", 210, "http://audio.url",
                "mp3", null, null, null, null, null,
                0L, true, null, LocalDateTime.now(), LocalDateTime.now(), null, null);
    }

    private Song buildInactiveSong() {
        return new Song(SONG_ID, "Inactive Song", "inactive-song", 210, "http://audio.url",
                "mp3", null, null, null, null, null,
                0L, false, null, LocalDateTime.now(), LocalDateTime.now(), null, null);
    }

    private Favorite buildFavorite() {
        return new Favorite(USER_ID, SONG_ID, LocalDateTime.now());
    }

    // ── addFavorite ─────────────────────────────────────────────────────────────

    @Test
    void addFavorite_WhenValid_ShouldCallRepoAdd() {
        when(userRepoPort.findByEmail(EMAIL)).thenReturn(Optional.of(buildUser()));
        when(songRepoPort.findById(SONG_ID)).thenReturn(Optional.of(buildActiveSong()));
        when(favoriteRepoPort.exists(USER_ID, SONG_ID)).thenReturn(false);

        favoriteService.addFavorite(EMAIL, SONG_ID);

        verify(favoriteRepoPort).add(USER_ID, SONG_ID);
    }

    @Test
    void addFavorite_WhenSongNotFound_ShouldThrow() {
        when(userRepoPort.findByEmail(EMAIL)).thenReturn(Optional.of(buildUser()));
        when(songRepoPort.findById(SONG_ID)).thenReturn(Optional.empty());

        assertThrows(SongNotFoundException.class, () -> favoriteService.addFavorite(EMAIL, SONG_ID));
        verify(favoriteRepoPort, never()).add(any(), any());
    }

    @Test
    void addFavorite_WhenSongInactive_ShouldThrow() {
        when(userRepoPort.findByEmail(EMAIL)).thenReturn(Optional.of(buildUser()));
        when(songRepoPort.findById(SONG_ID)).thenReturn(Optional.of(buildInactiveSong()));

        assertThrows(SongNotFoundException.class, () -> favoriteService.addFavorite(EMAIL, SONG_ID));
        verify(favoriteRepoPort, never()).add(any(), any());
    }

    @Test
    void addFavorite_WhenAlreadyExists_ShouldThrow() {
        when(userRepoPort.findByEmail(EMAIL)).thenReturn(Optional.of(buildUser()));
        when(songRepoPort.findById(SONG_ID)).thenReturn(Optional.of(buildActiveSong()));
        when(favoriteRepoPort.exists(USER_ID, SONG_ID)).thenReturn(true);

        assertThrows(FavoriteAlreadyExistsException.class, () -> favoriteService.addFavorite(EMAIL, SONG_ID));
        verify(favoriteRepoPort, never()).add(any(), any());
    }

    @Test
    void addFavorite_WhenUserNotFound_ShouldThrow() {
        when(userRepoPort.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> favoriteService.addFavorite(EMAIL, SONG_ID));
    }

    // ── removeFavorite ──────────────────────────────────────────────────────────

    @Test
    void removeFavorite_WhenValid_ShouldCallRepoRemove() {
        when(userRepoPort.findByEmail(EMAIL)).thenReturn(Optional.of(buildUser()));
        when(favoriteRepoPort.exists(USER_ID, SONG_ID)).thenReturn(true);

        favoriteService.removeFavorite(EMAIL, SONG_ID);

        verify(favoriteRepoPort).remove(USER_ID, SONG_ID);
    }

    @Test
    void removeFavorite_WhenNotInFavorites_ShouldThrow() {
        when(userRepoPort.findByEmail(EMAIL)).thenReturn(Optional.of(buildUser()));
        when(favoriteRepoPort.exists(USER_ID, SONG_ID)).thenReturn(false);

        assertThrows(FavoriteNotFoundException.class, () -> favoriteService.removeFavorite(EMAIL, SONG_ID));
        verify(favoriteRepoPort, never()).remove(any(), any());
    }

    // ── isFavorite ──────────────────────────────────────────────────────────────

    @Test
    void isFavorite_WhenExists_ShouldReturnTrue() {
        when(userRepoPort.findByEmail(EMAIL)).thenReturn(Optional.of(buildUser()));
        when(favoriteRepoPort.exists(USER_ID, SONG_ID)).thenReturn(true);

        assertTrue(favoriteService.isFavorite(EMAIL, SONG_ID));
    }

    @Test
    void isFavorite_WhenNotExists_ShouldReturnFalse() {
        when(userRepoPort.findByEmail(EMAIL)).thenReturn(Optional.of(buildUser()));
        when(favoriteRepoPort.exists(USER_ID, SONG_ID)).thenReturn(false);

        assertFalse(favoriteService.isFavorite(EMAIL, SONG_ID));
    }

    // ── getFavorites ────────────────────────────────────────────────────────────

    @Test
    void getFavorites_WhenValid_ShouldReturnPagedResult() {
        Favorite fav = buildFavorite();
        Song song = buildActiveSong();

        when(userRepoPort.findByEmail(EMAIL)).thenReturn(Optional.of(buildUser()));
        when(favoriteRepoPort.findByUserId(USER_ID, 0, 20)).thenReturn(List.of(fav));
        when(favoriteRepoPort.countByUserId(USER_ID)).thenReturn(1L);
        when(songRepoPort.findByIds(List.of(SONG_ID))).thenReturn(List.of(song));
        when(songArtistRepoPort.findArtistIdsBySongIds(List.of(SONG_ID))).thenReturn(Map.of(SONG_ID, Collections.emptyList()));
        when(songGenreRepoPort.findGenreIdsBySongIds(List.of(SONG_ID))).thenReturn(Map.of(SONG_ID, Collections.emptyList()));
        when(artistRepoPort.findByIds(Collections.emptyList())).thenReturn(Collections.emptyList());
        when(genreRepoPort.findByIds(Collections.emptyList())).thenReturn(Collections.emptyList());

        PageResultDto<FavoriteSongResponse> result = favoriteService.getFavorites(EMAIL, 0, 20);

        assertNotNull(result);
        assertEquals(1, result.getItems().size());
        assertEquals(1L, result.getTotalElements());
        assertEquals(SONG_ID, result.getItems().get(0).getSongId());
    }

    @Test
    void getFavorites_WhenEmpty_ShouldReturnEmptyPage() {
        when(userRepoPort.findByEmail(EMAIL)).thenReturn(Optional.of(buildUser()));
        when(favoriteRepoPort.findByUserId(USER_ID, 0, 20)).thenReturn(Collections.emptyList());
        when(favoriteRepoPort.countByUserId(USER_ID)).thenReturn(0L);

        PageResultDto<FavoriteSongResponse> result = favoriteService.getFavorites(EMAIL, 0, 20);

        assertNotNull(result);
        assertTrue(result.getItems().isEmpty());
        assertEquals(0L, result.getTotalElements());
    }
}
