package com.example.ondas_be.integration.controller;

import com.example.ondas_be.application.dto.common.PageResultDto;
import com.example.ondas_be.application.dto.request.AddSongToPlaylistRequest;
import com.example.ondas_be.application.dto.request.CreatePlaylistRequest;
import com.example.ondas_be.application.dto.request.PlaylistFilterRequest;
import com.example.ondas_be.application.dto.request.ReorderPlaylistSongsRequest;
import com.example.ondas_be.application.dto.request.UpdatePlaylistRequest;
import com.example.ondas_be.application.dto.response.PlaylistResponse;
import com.example.ondas_be.application.exception.PlaylistAccessDeniedException;
import com.example.ondas_be.application.exception.PlaylistNotFoundException;
import com.example.ondas_be.application.exception.PlaylistReorderInvalidException;
import com.example.ondas_be.application.exception.PlaylistSongAlreadyExistsException;
import com.example.ondas_be.application.exception.PlaylistSongNotFoundException;
import com.example.ondas_be.application.exception.SongNotFoundException;
import com.example.ondas_be.application.service.port.PlaylistServicePort;
import com.example.ondas_be.infrastructure.security.JwtUtil;
import com.example.ondas_be.infrastructure.security.SecurityConfig;
import com.example.ondas_be.infrastructure.security.UserDetailsServiceImpl;
import com.example.ondas_be.presentation.advice.GlobalExceptionHandler;
import com.example.ondas_be.presentation.controller.PlaylistController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PlaylistController.class)
@AutoConfigureMockMvc
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
class PlaylistControllerTest {

    @Autowired
    private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

    @MockitoBean
    private PlaylistServicePort playlistServicePort;

        @MockitoBean
        private JwtUtil jwtUtil;

        @MockitoBean
        private UserDetailsServiceImpl userDetailsService;

    @Test
        @WithMockUser(roles = "USER")
    void getPlaylists_ShouldReturn200_WhenPublicFilter() throws Exception {
        PlaylistResponse item = PlaylistResponse.builder()
                .id(UUID.randomUUID())
                .name("Public Playlist")
                .isPublic(true)
                .totalSongs(2)
                .songs(List.of())
                .build();

        PageResultDto<PlaylistResponse> page = PageResultDto.<PlaylistResponse>builder()
                .items(List.of(item))
                .page(0)
                .size(20)
                .totalElements(1)
                .totalPages(1)
                .build();

        when(playlistServicePort.getPlaylists(any(), any(PlaylistFilterRequest.class))).thenReturn(page);

        mockMvc.perform(get("/api/playlists"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items[0].name").value("Public Playlist"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getPlaylists_ShouldReturn200_WhenOwnerFilter() throws Exception {
        PlaylistResponse item = PlaylistResponse.builder()
                .id(UUID.randomUUID())
                .name("My Playlist")
                .isPublic(false)
                .totalSongs(0)
                .songs(List.of())
                .build();

        PageResultDto<PlaylistResponse> page = PageResultDto.<PlaylistResponse>builder()
                .items(List.of(item))
                .page(0)
                .size(20)
                .totalElements(1)
                .totalPages(1)
                .build();

        when(playlistServicePort.getPlaylists(any(), any(PlaylistFilterRequest.class))).thenReturn(page);

        mockMvc.perform(get("/api/playlists").param("owner", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items[0].name").value("My Playlist"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getPlaylists_ShouldReturn400_WhenPageIsNegative() throws Exception {
        when(playlistServicePort.getPlaylists(any(), any(PlaylistFilterRequest.class)))
                .thenThrow(new IllegalArgumentException("page must be >= 0"));

        mockMvc.perform(get("/api/playlists").param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("page must be >= 0"));
    }

    @Test
        @WithMockUser(roles = "USER")
    void getPlaylistById_ShouldReturn404_WhenNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(playlistServicePort.getPlaylistById(any(), eq(id)))
                .thenThrow(new PlaylistNotFoundException("Playlist not found with id: " + id));

        mockMvc.perform(get("/api/playlists/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getPlaylistById_ShouldReturn200_WhenPublicPlaylist() throws Exception {
        UUID id = UUID.randomUUID();
        PlaylistResponse item = PlaylistResponse.builder()
                .id(id)
                .name("Public Playlist")
                .isPublic(true)
                .totalSongs(1)
                .songs(List.of())
                .build();

        when(playlistServicePort.getPlaylistById(any(), eq(id))).thenReturn(item);

        mockMvc.perform(get("/api/playlists/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Public Playlist"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getPlaylistById_ShouldReturn403_WhenNotOwner() throws Exception {
        UUID id = UUID.randomUUID();
        when(playlistServicePort.getPlaylistById(any(), eq(id)))
                .thenThrow(new PlaylistAccessDeniedException("Not owner"));

        mockMvc.perform(get("/api/playlists/{id}", id))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void createPlaylist_ShouldReturn401_WhenNotAuthenticated() throws Exception {
        MockMultipartFile data = new MockMultipartFile(
                "data",
                "data",
                MediaType.APPLICATION_JSON_VALUE,
                "{\"name\":\"My Playlist\"}".getBytes());

        mockMvc.perform(multipart("/api/playlists").file(data))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void createPlaylist_ShouldReturn201_WhenValid() throws Exception {
        PlaylistResponse response = PlaylistResponse.builder()
                .id(UUID.randomUUID())
                .name("My Playlist")
                .isPublic(false)
                .totalSongs(0)
                .songs(List.of())
                .build();

        when(playlistServicePort.createPlaylist(any(), any(CreatePlaylistRequest.class), any()))
                .thenReturn(response);

        CreatePlaylistRequest request = new CreatePlaylistRequest();
        request.setName("My Playlist");

        MockMultipartFile data = new MockMultipartFile(
                "data",
                "data",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request));

        mockMvc.perform(multipart("/api/playlists").file(data))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "USER")
    void createPlaylist_ShouldReturn400_WhenNameBlank() throws Exception {
        CreatePlaylistRequest request = new CreatePlaylistRequest();
        request.setName(" ");

        MockMultipartFile data = new MockMultipartFile(
                "data",
                "data",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request));

        mockMvc.perform(multipart("/api/playlists").file(data))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void updatePlaylist_ShouldReturn401_WhenNotAuthenticated() throws Exception {
        UUID id = UUID.randomUUID();
        MockMultipartFile data = new MockMultipartFile(
                "data",
                "data",
                MediaType.APPLICATION_JSON_VALUE,
                "{\"name\":\"New\"}".getBytes());
        mockMvc.perform(multipart("/api/playlists/{id}", id)
                        .file(data)
                        .with(request -> { request.setMethod("PUT"); return request; }))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void updatePlaylist_ShouldReturn200_WhenPartialUpdate() throws Exception {
        UUID id = UUID.randomUUID();
        PlaylistResponse response = PlaylistResponse.builder()
                .id(id)
                .name("Updated")
                .isPublic(true)
                .totalSongs(0)
                .songs(List.of())
                .build();

        when(playlistServicePort.updatePlaylist(any(), eq(id), any(UpdatePlaylistRequest.class), any()))
                .thenReturn(response);

        UpdatePlaylistRequest request = new UpdatePlaylistRequest();
        request.setName("Updated");

        MockMultipartFile data = new MockMultipartFile(
                "data",
                "data",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request));

        mockMvc.perform(multipart("/api/playlists/{id}", id)
                        .file(data)
                        .with(req -> { req.setMethod("PUT"); return req; }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Updated"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void updatePlaylist_ShouldReturn404_WhenNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(playlistServicePort.updatePlaylist(any(), eq(id), any(UpdatePlaylistRequest.class), any()))
                .thenThrow(new PlaylistNotFoundException("Playlist not found"));

        MockMultipartFile data = new MockMultipartFile(
                "data",
                "data",
                MediaType.APPLICATION_JSON_VALUE,
                "{\"name\":\"Updated\"}".getBytes());

        mockMvc.perform(multipart("/api/playlists/{id}", id)
                        .file(data)
                        .with(req -> { req.setMethod("PUT"); return req; }))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "USER")
    void updatePlaylist_ShouldReturn403_WhenNotOwner() throws Exception {
        UUID id = UUID.randomUUID();
        when(playlistServicePort.updatePlaylist(any(), eq(id), any(UpdatePlaylistRequest.class), any()))
                .thenThrow(new PlaylistAccessDeniedException("Not owner"));

        MockMultipartFile data = new MockMultipartFile(
                "data",
                "data",
                MediaType.APPLICATION_JSON_VALUE,
                "{\"name\":\"Updated\"}".getBytes());

        mockMvc.perform(multipart("/api/playlists/{id}", id)
                        .file(data)
                        .with(req -> { req.setMethod("PUT"); return req; }))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "USER")
    void deletePlaylist_ShouldReturn200_WhenOwner() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/playlists/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "USER")
    void deletePlaylist_ShouldReturn404_WhenNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new PlaylistNotFoundException("Playlist not found"))
                .when(playlistServicePort).deletePlaylist(any(), eq(id));

        mockMvc.perform(delete("/api/playlists/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "USER")
    void deletePlaylist_ShouldReturn403_WhenNotOwner() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new PlaylistAccessDeniedException("Not owner"))
                .when(playlistServicePort).deletePlaylist(any(), eq(id));

        mockMvc.perform(delete("/api/playlists/{id}", id))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "USER")
    void addSongToPlaylist_ShouldReturn200_WhenValid() throws Exception {
        UUID id = UUID.randomUUID();
        AddSongToPlaylistRequest request = new AddSongToPlaylistRequest();
        request.setSongId(UUID.randomUUID());

        PlaylistResponse response = PlaylistResponse.builder()
                .id(id)
                .name("My Playlist")
                .isPublic(false)
                .totalSongs(1)
                .songs(List.of())
                .build();

        when(playlistServicePort.addSongToPlaylist(any(), eq(id), any(AddSongToPlaylistRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/playlists/{id}/songs", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalSongs").value(1));
    }

    @Test
    @WithMockUser(roles = "USER")
    void addSongToPlaylist_ShouldReturn409_WhenDuplicate() throws Exception {
        UUID id = UUID.randomUUID();
        AddSongToPlaylistRequest request = new AddSongToPlaylistRequest();
        request.setSongId(UUID.randomUUID());

        when(playlistServicePort.addSongToPlaylist(any(), eq(id), any(AddSongToPlaylistRequest.class)))
                .thenThrow(new PlaylistSongAlreadyExistsException("Song already exists"));

        mockMvc.perform(post("/api/playlists/{id}/songs", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "USER")
    void addSongToPlaylist_ShouldReturn404_WhenPlaylistNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        AddSongToPlaylistRequest request = new AddSongToPlaylistRequest();
        request.setSongId(UUID.randomUUID());

        when(playlistServicePort.addSongToPlaylist(any(), eq(id), any(AddSongToPlaylistRequest.class)))
                .thenThrow(new PlaylistNotFoundException("Playlist not found"));

        mockMvc.perform(post("/api/playlists/{id}/songs", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "USER")
    void addSongToPlaylist_ShouldReturn404_WhenSongNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        AddSongToPlaylistRequest request = new AddSongToPlaylistRequest();
        request.setSongId(UUID.randomUUID());

        when(playlistServicePort.addSongToPlaylist(any(), eq(id), any(AddSongToPlaylistRequest.class)))
                .thenThrow(new SongNotFoundException("Song not found"));

        mockMvc.perform(post("/api/playlists/{id}/songs", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "USER")
    void addSongToPlaylist_ShouldReturn403_WhenNotOwner() throws Exception {
        UUID id = UUID.randomUUID();
        AddSongToPlaylistRequest request = new AddSongToPlaylistRequest();
        request.setSongId(UUID.randomUUID());

        when(playlistServicePort.addSongToPlaylist(any(), eq(id), any(AddSongToPlaylistRequest.class)))
                .thenThrow(new PlaylistAccessDeniedException("Not owner"));

        mockMvc.perform(post("/api/playlists/{id}/songs", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "USER")
    void removeSongFromPlaylist_ShouldReturn200_WhenValid() throws Exception {
        UUID id = UUID.randomUUID();
        UUID songId = UUID.randomUUID();
        PlaylistResponse response = PlaylistResponse.builder()
                .id(id)
                .name("My Playlist")
                .isPublic(false)
                .totalSongs(0)
                .songs(List.of())
                .build();

        when(playlistServicePort.removeSongFromPlaylist(any(), eq(id), eq(songId))).thenReturn(response);

        mockMvc.perform(delete("/api/playlists/{id}/songs/{songId}", id, songId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "USER")
    void removeSongFromPlaylist_ShouldReturn404_WhenSongNotInPlaylist() throws Exception {
        UUID id = UUID.randomUUID();
        UUID songId = UUID.randomUUID();

        when(playlistServicePort.removeSongFromPlaylist(any(), eq(id), eq(songId)))
                .thenThrow(new PlaylistSongNotFoundException("Song not found in playlist"));

        mockMvc.perform(delete("/api/playlists/{id}/songs/{songId}", id, songId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "USER")
    void removeSongFromPlaylist_ShouldReturn404_WhenPlaylistNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        UUID songId = UUID.randomUUID();

        when(playlistServicePort.removeSongFromPlaylist(any(), eq(id), eq(songId)))
                .thenThrow(new PlaylistNotFoundException("Playlist not found"));

        mockMvc.perform(delete("/api/playlists/{id}/songs/{songId}", id, songId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "USER")
    void removeSongFromPlaylist_ShouldReturn403_WhenNotOwner() throws Exception {
        UUID id = UUID.randomUUID();
        UUID songId = UUID.randomUUID();

        when(playlistServicePort.removeSongFromPlaylist(any(), eq(id), eq(songId)))
                .thenThrow(new PlaylistAccessDeniedException("Not owner"));

        mockMvc.perform(delete("/api/playlists/{id}/songs/{songId}", id, songId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "USER")
    void reorderPlaylistSongs_ShouldReturn200_WhenValid() throws Exception {
        UUID id = UUID.randomUUID();
        ReorderPlaylistSongsRequest request = new ReorderPlaylistSongsRequest();
        request.setSongIds(List.of(UUID.randomUUID(), UUID.randomUUID()));

        PlaylistResponse response = PlaylistResponse.builder()
                .id(id)
                .name("My Playlist")
                .isPublic(false)
                .totalSongs(2)
                .songs(List.of())
                .build();

        when(playlistServicePort.reorderPlaylistSongs(any(), eq(id), any(ReorderPlaylistSongsRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/playlists/{id}/songs/reorder", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "USER")
    void reorderPlaylistSongs_ShouldReturn400_WhenDuplicateSongs() throws Exception {
        UUID id = UUID.randomUUID();
        ReorderPlaylistSongsRequest request = new ReorderPlaylistSongsRequest();
        UUID songId = UUID.randomUUID();
        request.setSongIds(List.of(songId, songId));

        when(playlistServicePort.reorderPlaylistSongs(any(), eq(id), any(ReorderPlaylistSongsRequest.class)))
                .thenThrow(new PlaylistReorderInvalidException("Duplicate song IDs"));

        mockMvc.perform(put("/api/playlists/{id}/songs/reorder", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "USER")
    void reorderPlaylistSongs_ShouldReturn400_WhenMissingSong() throws Exception {
        UUID id = UUID.randomUUID();
        ReorderPlaylistSongsRequest request = new ReorderPlaylistSongsRequest();
        request.setSongIds(List.of(UUID.randomUUID()));

        when(playlistServicePort.reorderPlaylistSongs(any(), eq(id), any(ReorderPlaylistSongsRequest.class)))
                .thenThrow(new PlaylistReorderInvalidException("Missing songs"));

        mockMvc.perform(put("/api/playlists/{id}/songs/reorder", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "USER")
    void reorderPlaylistSongs_ShouldReturn404_WhenPlaylistNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        ReorderPlaylistSongsRequest request = new ReorderPlaylistSongsRequest();
        request.setSongIds(List.of(UUID.randomUUID()));

        when(playlistServicePort.reorderPlaylistSongs(any(), eq(id), any(ReorderPlaylistSongsRequest.class)))
                .thenThrow(new PlaylistNotFoundException("Playlist not found"));

        mockMvc.perform(put("/api/playlists/{id}/songs/reorder", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "USER")
    void reorderPlaylistSongs_ShouldReturn403_WhenNotOwner() throws Exception {
        UUID id = UUID.randomUUID();
        ReorderPlaylistSongsRequest request = new ReorderPlaylistSongsRequest();
        request.setSongIds(List.of(UUID.randomUUID()));

        when(playlistServicePort.reorderPlaylistSongs(any(), eq(id), any(ReorderPlaylistSongsRequest.class)))
                .thenThrow(new PlaylistAccessDeniedException("Not owner"));

        mockMvc.perform(put("/api/playlists/{id}/songs/reorder", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }
}
