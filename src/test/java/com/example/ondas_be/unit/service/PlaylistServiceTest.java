package com.example.ondas_be.unit.service;

import com.example.ondas_be.application.dto.request.AddSongToPlaylistRequest;
import com.example.ondas_be.application.dto.request.CreatePlaylistRequest;
import com.example.ondas_be.application.dto.request.ReorderPlaylistSongsRequest;
import com.example.ondas_be.application.dto.response.PlaylistResponse;
import com.example.ondas_be.application.exception.PlaylistAccessDeniedException;
import com.example.ondas_be.application.exception.PlaylistReorderInvalidException;
import com.example.ondas_be.application.exception.PlaylistSongAlreadyExistsException;
import com.example.ondas_be.application.mapper.PlaylistMapper;
import com.example.ondas_be.application.service.impl.PlaylistService;
import com.example.ondas_be.application.service.port.StoragePort;
import com.example.ondas_be.domain.entity.Playlist;
import com.example.ondas_be.domain.entity.Song;
import com.example.ondas_be.domain.entity.User;
import com.example.ondas_be.domain.repoport.PlaylistRepoPort;
import com.example.ondas_be.domain.repoport.PlaylistSongRepoPort;
import com.example.ondas_be.domain.repoport.SongRepoPort;
import com.example.ondas_be.domain.repoport.UserRepoPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlaylistServiceTest {

    @Mock
    private PlaylistRepoPort playlistRepoPort;

    @Mock
    private PlaylistSongRepoPort playlistSongRepoPort;

    @Mock
    private SongRepoPort songRepoPort;

    @Mock
    private UserRepoPort userRepoPort;

    @Mock
    private StoragePort storagePort;

    @Mock
    private PlaylistMapper playlistMapper;

    @InjectMocks
    private PlaylistService playlistService;

    private UUID userId;
    private UUID playlistId;
    private User user;
    private Playlist playlist;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(playlistService, "imageBucket", "ondas-images");

        userId = UUID.randomUUID();
        playlistId = UUID.randomUUID();

        user = new User(
                userId,
                "user@example.com",
                "hash",
                "User",
                null,
                true,
                null,
                null,
                null,
                com.example.ondas_be.domain.entity.Role.USER,
                LocalDateTime.now(),
                LocalDateTime.now());

        playlist = new Playlist(
                playlistId,
                userId,
                "My Playlist",
                "desc",
                null,
                false,
                0,
                LocalDateTime.now(),
                LocalDateTime.now());

        lenient().when(playlistMapper.toResponse(any(Playlist.class))).thenAnswer(invocation -> {
            Playlist input = invocation.getArgument(0);
            return PlaylistResponse.builder()
                    .id(input.getId())
                    .userId(input.getUserId())
                    .name(input.getName())
                    .description(input.getDescription())
                    .coverUrl(input.getCoverUrl())
                    .isPublic(input.isPublic())
                    .totalSongs(input.getTotalSongs())
                    .createdAt(input.getCreatedAt())
                    .updatedAt(input.getUpdatedAt())
                    .songs(List.of())
                    .build();
        });
        lenient().when(songRepoPort.findByIds(anyList())).thenReturn(List.of());
    }

    @Test
    void createPlaylist_ShouldSaveOwnerPlaylist() {
        CreatePlaylistRequest request = new CreatePlaylistRequest();
        request.setName(" Chill ");
        request.setDescription("desc");
        request.setIsPublic(true);

        when(userRepoPort.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(playlistRepoPort.save(any(Playlist.class))).thenAnswer(invocation -> {
            Playlist input = invocation.getArgument(0);
            return new Playlist(
                    playlistId,
                    input.getUserId(),
                    input.getName(),
                    input.getDescription(),
                    input.getCoverUrl(),
                    input.isPublic(),
                    0,
                    LocalDateTime.now(),
                    LocalDateTime.now());
        });
        when(playlistSongRepoPort.findByPlaylistIdOrderByPosition(playlistId)).thenReturn(List.of());

        PlaylistResponse response = playlistService.createPlaylist("user@example.com", request, null);

        assertEquals("Chill", response.getName());
        assertEquals(userId, response.getUserId());
        verify(playlistRepoPort).save(any(Playlist.class));
    }

    @Test
    void addSongToPlaylist_WhenSongAlreadyExists_ShouldThrowConflict() {
        AddSongToPlaylistRequest request = new AddSongToPlaylistRequest();
        UUID songId = UUID.randomUUID();
        request.setSongId(songId);

        when(userRepoPort.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(playlistRepoPort.findById(playlistId)).thenReturn(Optional.of(playlist));
        when(songRepoPort.findById(songId)).thenReturn(Optional.of(new Song(
                songId, "S", "s", 100, "url", "mp3", 100L, null,
                null, null, null, 0L, true, null, LocalDateTime.now(), LocalDateTime.now(), List.of(), List.of())));
        when(playlistSongRepoPort.existsByPlaylistIdAndSongId(playlistId, songId)).thenReturn(true);

        assertThrows(PlaylistSongAlreadyExistsException.class,
                () -> playlistService.addSongToPlaylist("user@example.com", playlistId, request));

        verify(playlistSongRepoPort, never()).save(any());
    }

    @Test
    void updatePlaylist_WhenRequesterIsNotOwner_ShouldThrowForbidden() {
        UUID otherUserId = UUID.randomUUID();
        User otherUser = new User(
                otherUserId,
                "other@example.com",
                "hash",
                "Other",
                null,
                true,
                null,
                null,
                null,
                com.example.ondas_be.domain.entity.Role.USER,
                LocalDateTime.now(),
                LocalDateTime.now());

        when(userRepoPort.findByEmail("other@example.com")).thenReturn(Optional.of(otherUser));
        when(playlistRepoPort.findById(playlistId)).thenReturn(Optional.of(playlist));

        assertThrows(PlaylistAccessDeniedException.class,
                () -> playlistService.deletePlaylist("other@example.com", playlistId));
    }

    @Test
    void reorderPlaylistSongs_WhenPayloadMissingSong_ShouldThrowBadRequest() {
        UUID song1 = UUID.randomUUID();
        UUID song2 = UUID.randomUUID();

        ReorderPlaylistSongsRequest request = new ReorderPlaylistSongsRequest();
        request.setSongIds(List.of(song1));

        when(userRepoPort.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(playlistRepoPort.findById(playlistId)).thenReturn(Optional.of(playlist));
        when(playlistSongRepoPort.findSongIdsByPlaylistId(playlistId)).thenReturn(List.of(song1, song2));

        assertThrows(PlaylistReorderInvalidException.class,
                () -> playlistService.reorderPlaylistSongs("user@example.com", playlistId, request));

        verify(playlistSongRepoPort, never()).updateSongOrder(eq(playlistId), anyList());
    }
}
