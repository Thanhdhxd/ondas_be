package com.example.ondas_be.integration.controller;

import com.example.ondas_be.application.dto.common.PageResultDto;
import com.example.ondas_be.application.dto.request.CreateSongRequest;
import com.example.ondas_be.application.dto.request.UpdateSongRequest;
import com.example.ondas_be.application.dto.request.SongTagRequest;
import com.example.ondas_be.application.dto.response.SongResponse;
import com.example.ondas_be.application.dto.response.SongStreamResponse;
import com.example.ondas_be.application.dto.response.TagResponse;
import com.example.ondas_be.application.exception.AlbumNotFoundException;
import com.example.ondas_be.application.exception.ArtistNotFoundException;
import com.example.ondas_be.application.exception.GenreNotFoundException;
import com.example.ondas_be.application.exception.SongNotFoundException;
import com.example.ondas_be.application.service.port.SongServicePort;
import com.example.ondas_be.infrastructure.security.JwtUtil;
import com.example.ondas_be.infrastructure.security.SecurityConfig;
import com.example.ondas_be.infrastructure.security.UserDetailsServiceImpl;
import com.example.ondas_be.presentation.advice.GlobalExceptionHandler;
import com.example.ondas_be.presentation.controller.SongController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SongController.class)
@AutoConfigureMockMvc
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
class SongControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SongServicePort songServicePort;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    private SongResponse buildSongResponse(UUID id) {
        return SongResponse.builder()
                .id(id)
                .title("Song A")
                .slug("song-a")
                .audioUrl("audio-url")
                .audioFormat("mp3")
                .audioSizeBytes(100L)
                .coverUrl("cover-url")
                .releaseDate(LocalDate.of(2024, 1, 1))
                .active(true)
                .build();
    }

        private TagResponse buildTagResponse(Long id) {
                return TagResponse.builder()
                                .id(id)
                                .name("Happy")
                                .type("mood")
                                .colorHex("#FF9900")
                                .build();
        }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createSong_ShouldReturn201_WhenValid() throws Exception {
        UUID id = UUID.randomUUID();
        when(songServicePort.createSong(any(CreateSongRequest.class), any(), any()))
                .thenReturn(buildSongResponse(id));

        CreateSongRequest request = new CreateSongRequest();
        request.setTitle("Song A");
        request.setArtistIds(List.of(UUID.randomUUID()));
        request.setGenreIds(List.of(1L));

        MockMultipartFile data = new MockMultipartFile(
                "data",
                "data",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request));
        MockMultipartFile audio = new MockMultipartFile(
                "audio",
                "audio.mp3",
                "audio/mpeg",
                "audio".getBytes());
        MockMultipartFile cover = new MockMultipartFile(
                "cover",
                "cover.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "img".getBytes());

        mockMvc.perform(multipart("/api/songs")
                        .file(data)
                        .file(audio)
                        .file(cover))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createSong_ShouldReturn201_WhenCoverMissing() throws Exception {
        UUID id = UUID.randomUUID();
        when(songServicePort.createSong(any(CreateSongRequest.class), any(), any()))
                .thenReturn(buildSongResponse(id));

        CreateSongRequest request = new CreateSongRequest();
        request.setTitle("Song A");
        request.setArtistIds(List.of(UUID.randomUUID()));
        request.setGenreIds(List.of(1L));

        MockMultipartFile data = new MockMultipartFile(
                "data",
                "data",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request));
        MockMultipartFile audio = new MockMultipartFile(
                "audio",
                "audio.mp3",
                "audio/mpeg",
                "audio".getBytes());

        mockMvc.perform(multipart("/api/songs")
                        .file(data)
                        .file(audio))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createSong_ShouldReturn400_WhenAudioMissing() throws Exception {
        CreateSongRequest request = new CreateSongRequest();
        request.setTitle("Song A");
        request.setArtistIds(List.of(UUID.randomUUID()));
        request.setGenreIds(List.of(1L));

        MockMultipartFile data = new MockMultipartFile(
                "data",
                "data",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request));

        mockMvc.perform(multipart("/api/songs")
                        .file(data))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createSong_ShouldReturn400_WhenTitleBlank() throws Exception {
        CreateSongRequest request = new CreateSongRequest();
        request.setTitle(" ");
        request.setArtistIds(List.of(UUID.randomUUID()));
        request.setGenreIds(List.of(1L));

        MockMultipartFile data = new MockMultipartFile(
                "data",
                "data",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request));
        MockMultipartFile audio = new MockMultipartFile(
                "audio",
                "audio.mp3",
                "audio/mpeg",
                "audio".getBytes());

        mockMvc.perform(multipart("/api/songs")
                        .file(data)
                        .file(audio))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createSong_ShouldReturn400_WhenArtistIdsEmpty() throws Exception {
        CreateSongRequest request = new CreateSongRequest();
        request.setTitle("Song A");
        request.setArtistIds(List.of());
        request.setGenreIds(List.of(1L));

        MockMultipartFile data = new MockMultipartFile(
                "data",
                "data",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request));
        MockMultipartFile audio = new MockMultipartFile(
                "audio",
                "audio.mp3",
                "audio/mpeg",
                "audio".getBytes());

        mockMvc.perform(multipart("/api/songs")
                        .file(data)
                        .file(audio))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createSong_ShouldReturn400_WhenGenreIdsEmpty() throws Exception {
        CreateSongRequest request = new CreateSongRequest();
        request.setTitle("Song A");
        request.setArtistIds(List.of(UUID.randomUUID()));
        request.setGenreIds(List.of());

        MockMultipartFile data = new MockMultipartFile(
                "data",
                "data",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request));
        MockMultipartFile audio = new MockMultipartFile(
                "audio",
                "audio.mp3",
                "audio/mpeg",
                "audio".getBytes());

        mockMvc.perform(multipart("/api/songs")
                        .file(data)
                        .file(audio))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createSong_ShouldReturn404_WhenAlbumNotFound() throws Exception {
        when(songServicePort.createSong(any(CreateSongRequest.class), any(), any()))
                .thenThrow(new AlbumNotFoundException("Album not found"));

        CreateSongRequest request = new CreateSongRequest();
        request.setTitle("Song A");
        request.setAlbumId(UUID.randomUUID());
        request.setArtistIds(List.of(UUID.randomUUID()));
        request.setGenreIds(List.of(1L));

        MockMultipartFile data = new MockMultipartFile(
                "data",
                "data",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request));
        MockMultipartFile audio = new MockMultipartFile(
                "audio",
                "audio.mp3",
                "audio/mpeg",
                "audio".getBytes());

        mockMvc.perform(multipart("/api/songs")
                        .file(data)
                        .file(audio))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createSong_ShouldReturn404_WhenArtistNotFound() throws Exception {
        when(songServicePort.createSong(any(CreateSongRequest.class), any(), any()))
                .thenThrow(new ArtistNotFoundException("Artist not found"));

        CreateSongRequest request = new CreateSongRequest();
        request.setTitle("Song A");
        request.setArtistIds(List.of(UUID.randomUUID()));
        request.setGenreIds(List.of(1L));

        MockMultipartFile data = new MockMultipartFile(
                "data",
                "data",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request));
        MockMultipartFile audio = new MockMultipartFile(
                "audio",
                "audio.mp3",
                "audio/mpeg",
                "audio".getBytes());

        mockMvc.perform(multipart("/api/songs")
                        .file(data)
                        .file(audio))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createSong_ShouldReturn404_WhenGenreNotFound() throws Exception {
        when(songServicePort.createSong(any(CreateSongRequest.class), any(), any()))
                .thenThrow(new GenreNotFoundException("Genre not found"));

        CreateSongRequest request = new CreateSongRequest();
        request.setTitle("Song A");
        request.setArtistIds(List.of(UUID.randomUUID()));
        request.setGenreIds(List.of(1L));

        MockMultipartFile data = new MockMultipartFile(
                "data",
                "data",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request));
        MockMultipartFile audio = new MockMultipartFile(
                "audio",
                "audio.mp3",
                "audio/mpeg",
                "audio".getBytes());

        mockMvc.perform(multipart("/api/songs")
                        .file(data)
                        .file(audio))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createSong_ShouldReturn409_WhenSlugDuplicate() throws Exception {
        when(songServicePort.createSong(any(CreateSongRequest.class), any(), any()))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        CreateSongRequest request = new CreateSongRequest();
        request.setTitle("Song A");
        request.setArtistIds(List.of(UUID.randomUUID()));
        request.setGenreIds(List.of(1L));

        MockMultipartFile data = new MockMultipartFile(
                "data",
                "data",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request));
        MockMultipartFile audio = new MockMultipartFile(
                "audio",
                "audio.mp3",
                "audio/mpeg",
                "audio".getBytes());

        mockMvc.perform(multipart("/api/songs")
                        .file(data)
                        .file(audio))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "USER")
    void createSong_ShouldReturn403_WhenRoleNotAllowed() throws Exception {
        CreateSongRequest request = new CreateSongRequest();
        request.setTitle("Song A");
        request.setArtistIds(List.of(UUID.randomUUID()));
        request.setGenreIds(List.of(1L));

        MockMultipartFile data = new MockMultipartFile(
                "data",
                "data",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request));
        MockMultipartFile audio = new MockMultipartFile(
                "audio",
                "audio.mp3",
                "audio/mpeg",
                "audio".getBytes());

        mockMvc.perform(multipart("/api/songs")
                        .file(data)
                        .file(audio))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateSong_ShouldReturn200_WhenValid() throws Exception {
        UUID id = UUID.randomUUID();
        when(songServicePort.updateSong(eq(id), any(UpdateSongRequest.class), any(), any()))
                .thenReturn(buildSongResponse(id));

        UpdateSongRequest request = new UpdateSongRequest();
        request.setTitle("Song A");

        MockMultipartFile data = new MockMultipartFile(
                "data",
                "data",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request));
        MockMultipartFile audio = new MockMultipartFile(
                "audio",
                "audio.mp3",
                "audio/mpeg",
                "audio".getBytes());
        MockMultipartFile cover = new MockMultipartFile(
                "cover",
                "cover.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "img".getBytes());

        mockMvc.perform(multipart("/api/songs/{id}", id)
                        .file(data)
                        .file(audio)
                        .file(cover)
                        .with(req -> { req.setMethod("PUT"); return req; }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateSong_ShouldReturn200_WhenMetadataOnly() throws Exception {
        UUID id = UUID.randomUUID();
        when(songServicePort.updateSong(eq(id), any(UpdateSongRequest.class), any(), any()))
                .thenReturn(buildSongResponse(id));

        UpdateSongRequest request = new UpdateSongRequest();
        request.setTitle("Song A");

        MockMultipartFile data = new MockMultipartFile(
                "data",
                "data",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request));

        mockMvc.perform(multipart("/api/songs/{id}", id)
                        .file(data)
                        .with(req -> { req.setMethod("PUT"); return req; }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateSong_ShouldReturn404_WhenNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(songServicePort.updateSong(eq(id), any(UpdateSongRequest.class), any(), any()))
                .thenThrow(new SongNotFoundException("Song not found"));

        MockMultipartFile data = new MockMultipartFile(
                "data",
                "data",
                MediaType.APPLICATION_JSON_VALUE,
                "{\"title\":\"Song A\"}".getBytes());

        mockMvc.perform(multipart("/api/songs/{id}", id)
                        .file(data)
                        .with(req -> { req.setMethod("PUT"); return req; }))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateSong_ShouldReturn404_WhenArtistInvalid() throws Exception {
        UUID id = UUID.randomUUID();
        when(songServicePort.updateSong(eq(id), any(UpdateSongRequest.class), any(), any()))
                .thenThrow(new ArtistNotFoundException("Artist not found"));

        MockMultipartFile data = new MockMultipartFile(
                "data",
                "data",
                MediaType.APPLICATION_JSON_VALUE,
                "{\"title\":\"Song A\"}".getBytes());

        mockMvc.perform(multipart("/api/songs/{id}", id)
                        .file(data)
                        .with(req -> { req.setMethod("PUT"); return req; }))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateSong_ShouldReturn404_WhenGenreInvalid() throws Exception {
        UUID id = UUID.randomUUID();
        when(songServicePort.updateSong(eq(id), any(UpdateSongRequest.class), any(), any()))
                .thenThrow(new GenreNotFoundException("Genre not found"));

        MockMultipartFile data = new MockMultipartFile(
                "data",
                "data",
                MediaType.APPLICATION_JSON_VALUE,
                "{\"title\":\"Song A\"}".getBytes());

        mockMvc.perform(multipart("/api/songs/{id}", id)
                        .file(data)
                        .with(req -> { req.setMethod("PUT"); return req; }))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateSong_ShouldReturn409_WhenSlugDuplicate() throws Exception {
        UUID id = UUID.randomUUID();
        when(songServicePort.updateSong(eq(id), any(UpdateSongRequest.class), any(), any()))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        MockMultipartFile data = new MockMultipartFile(
                "data",
                "data",
                MediaType.APPLICATION_JSON_VALUE,
                "{\"title\":\"Song A\"}".getBytes());

        mockMvc.perform(multipart("/api/songs/{id}", id)
                        .file(data)
                        .with(req -> { req.setMethod("PUT"); return req; }))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateSong_ShouldReturn403_WhenRoleNotAllowed() throws Exception {
        UUID id = UUID.randomUUID();
        MockMultipartFile data = new MockMultipartFile(
                "data",
                "data",
                MediaType.APPLICATION_JSON_VALUE,
                "{\"title\":\"Song A\"}".getBytes());

        mockMvc.perform(multipart("/api/songs/{id}", id)
                        .file(data)
                        .with(req -> { req.setMethod("PUT"); return req; }))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getSongById_ShouldReturn200_WhenExists() throws Exception {
        UUID id = UUID.randomUUID();
        when(songServicePort.getSongById(id)).thenReturn(buildSongResponse(id));

        mockMvc.perform(get("/api/songs/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getSongById_ShouldReturn404_WhenNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(songServicePort.getSongById(id)).thenThrow(new SongNotFoundException("Song not found"));

        mockMvc.perform(get("/api/songs/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getSongs_ShouldReturn200_WhenFilterProvided() throws Exception {
        PageResultDto<SongResponse> page = PageResultDto.<SongResponse>builder()
                .items(List.of(buildSongResponse(UUID.randomUUID())))
                .page(0)
                .size(20)
                .totalElements(1)
                .totalPages(1)
                .build();

        when(songServicePort.getSongs(any())).thenReturn(page);

        mockMvc.perform(get("/api/songs").param("query", "love"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getSongs_ShouldReturn400_WhenPageNegative() throws Exception {
        when(songServicePort.getSongs(any())).thenThrow(new IllegalArgumentException("page must be >= 0"));

        mockMvc.perform(get("/api/songs").param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteSong_ShouldReturn200_WhenValid() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(songServicePort).deleteSong(id);

        mockMvc.perform(delete("/api/songs/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteSong_ShouldReturn404_WhenNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new SongNotFoundException("Song not found")).when(songServicePort).deleteSong(id);

        mockMvc.perform(delete("/api/songs/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteSong_ShouldReturn403_WhenRoleNotAllowed() throws Exception {
        mockMvc.perform(delete("/api/songs/{id}", UUID.randomUUID()))
                .andExpect(status().isForbidden());
    }

        @Test
        @WithMockUser(roles = "USER")
        void getSongTags_ShouldReturn200_WhenValid() throws Exception {
                UUID id = UUID.randomUUID();
                when(songServicePort.getSongTags(id)).thenReturn(List.of(buildTagResponse(1L)));

                mockMvc.perform(get("/api/songs/{id}/tags", id))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void addSongTags_ShouldReturn200_WhenAdmin() throws Exception {
                UUID id = UUID.randomUUID();
                when(songServicePort.addSongTags(eq(id), any())).thenReturn(List.of(buildTagResponse(1L)));

                SongTagRequest request = new SongTagRequest();
                request.setTagIds(List.of(1L));

                mockMvc.perform(post("/api/songs/{id}/tags", id)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsBytes(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @WithMockUser(roles = "USER")
        void addSongTags_ShouldReturn403_WhenRoleNotAllowed() throws Exception {
                SongTagRequest request = new SongTagRequest();
                request.setTagIds(List.of(1L));

                mockMvc.perform(post("/api/songs/{id}/tags", UUID.randomUUID())
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsBytes(request)))
                                .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void removeSongTags_ShouldReturn200_WhenAdmin() throws Exception {
                UUID id = UUID.randomUUID();
                when(songServicePort.removeSongTags(eq(id), any())).thenReturn(List.of());

                SongTagRequest request = new SongTagRequest();
                request.setTagIds(List.of(1L));

                mockMvc.perform(delete("/api/songs/{id}/tags", id)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsBytes(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void replaceSongTags_ShouldReturn200_WhenAdmin() throws Exception {
                UUID id = UUID.randomUUID();
                when(songServicePort.replaceSongTags(eq(id), any())).thenReturn(List.of(buildTagResponse(1L)));

                SongTagRequest request = new SongTagRequest();
                request.setTagIds(List.of(1L, 2L));

                mockMvc.perform(put("/api/songs/{id}/tags", id)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsBytes(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true));
        }

    @Test
    @WithMockUser(roles = "USER")
    void streamSong_ShouldReturn200_WhenFullContent() throws Exception {
        UUID id = UUID.randomUUID();
        SongStreamResponse stream = new SongStreamResponse(
                new ByteArrayInputStream("audio".getBytes()),
                5,
                0,
                4,
                "audio/mpeg",
                false
        );

        when(songServicePort.streamSong(eq(id), any())).thenReturn(stream);

        mockMvc.perform(get("/api/songs/{id}/stream", id))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCEPT_RANGES, "bytes"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void streamSong_ShouldReturn206_WhenRangeProvided() throws Exception {
        UUID id = UUID.randomUUID();
        SongStreamResponse stream = new SongStreamResponse(
                new ByteArrayInputStream("audio".getBytes()),
                10,
                0,
                4,
                "audio/mpeg",
                true
        );

        when(songServicePort.streamSong(eq(id), any())).thenReturn(stream);

        mockMvc.perform(get("/api/songs/{id}/stream", id)
                        .header(HttpHeaders.RANGE, "bytes=0-4"))
                .andExpect(status().isPartialContent())
                .andExpect(header().string(HttpHeaders.CONTENT_RANGE, "bytes 0-4/10"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void streamSong_ShouldReturn400_WhenRangeInvalid() throws Exception {
        UUID id = UUID.randomUUID();
        when(songServicePort.streamSong(eq(id), any()))
                .thenThrow(new IllegalArgumentException("Invalid range"));

        mockMvc.perform(get("/api/songs/{id}/stream", id)
                        .header(HttpHeaders.RANGE, "bytes=invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
