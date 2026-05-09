package com.example.ondas_be.integration.controller;

import com.example.ondas_be.application.dto.common.PageResultDto;
import com.example.ondas_be.application.dto.request.CreateAlbumRequest;
import com.example.ondas_be.application.dto.request.UpdateAlbumRequest;
import com.example.ondas_be.application.dto.response.AlbumResponse;
import com.example.ondas_be.application.exception.AlbumNotFoundException;
import com.example.ondas_be.application.exception.ArtistNotFoundException;
import com.example.ondas_be.application.service.port.AlbumServicePort;
import com.example.ondas_be.infrastructure.security.JwtUtil;
import com.example.ondas_be.infrastructure.security.SecurityConfig;
import com.example.ondas_be.infrastructure.security.UserDetailsServiceImpl;
import com.example.ondas_be.presentation.advice.GlobalExceptionHandler;
import com.example.ondas_be.presentation.controller.AlbumController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AlbumController.class)
@AutoConfigureMockMvc
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
class AlbumControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AlbumServicePort albumServicePort;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    private AlbumResponse buildAlbumResponse(UUID id) {
        return AlbumResponse.builder()
                .id(id)
                .title("Album A")
                .slug("album-a")
                .coverUrl("cover-url")
                .releaseDate(LocalDate.of(2024, 1, 1))
                .albumType("album")
                .description("desc")
                .totalTracks(1)
                .artistIds(List.of(UUID.randomUUID()))
                .artists(List.of())
                .tracklist(List.of())
                .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createAlbum_ShouldReturn201_WhenValid() throws Exception {
        UUID id = UUID.randomUUID();
        when(albumServicePort.createAlbum(any(CreateAlbumRequest.class), any()))
                .thenReturn(buildAlbumResponse(id));

        CreateAlbumRequest request = new CreateAlbumRequest();
        request.setTitle("Album A");
        request.setArtistIds(List.of(UUID.randomUUID()));

        MockMultipartFile data = new MockMultipartFile(
                "data",
                "data",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request));
        MockMultipartFile cover = new MockMultipartFile(
                "cover",
                "cover.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "img".getBytes());

        mockMvc.perform(multipart("/api/albums")
                        .file(data)
                        .file(cover))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Album A"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createAlbum_ShouldReturn201_WhenCoverMissing() throws Exception {
        UUID id = UUID.randomUUID();
        when(albumServicePort.createAlbum(any(CreateAlbumRequest.class), any()))
                .thenReturn(buildAlbumResponse(id));

        CreateAlbumRequest request = new CreateAlbumRequest();
        request.setTitle("Album A");
        request.setArtistIds(List.of(UUID.randomUUID()));

        MockMultipartFile data = new MockMultipartFile(
                "data",
                "data",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request));

        mockMvc.perform(multipart("/api/albums")
                        .file(data))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createAlbum_ShouldReturn400_WhenTitleBlank() throws Exception {
        CreateAlbumRequest request = new CreateAlbumRequest();
        request.setTitle(" ");
        request.setArtistIds(List.of(UUID.randomUUID()));

        MockMultipartFile data = new MockMultipartFile(
                "data",
                "data",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request));

        mockMvc.perform(multipart("/api/albums")
                        .file(data))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createAlbum_ShouldReturn400_WhenArtistIdsEmpty() throws Exception {
        CreateAlbumRequest request = new CreateAlbumRequest();
        request.setTitle("Album A");
        request.setArtistIds(List.of());

        MockMultipartFile data = new MockMultipartFile(
                "data",
                "data",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request));

        mockMvc.perform(multipart("/api/albums")
                        .file(data))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createAlbum_ShouldReturn404_WhenArtistNotFound() throws Exception {
        when(albumServicePort.createAlbum(any(CreateAlbumRequest.class), any()))
                .thenThrow(new ArtistNotFoundException("Artist not found"));

        CreateAlbumRequest request = new CreateAlbumRequest();
        request.setTitle("Album A");
        request.setArtistIds(List.of(UUID.randomUUID()));

        MockMultipartFile data = new MockMultipartFile(
                "data",
                "data",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request));

        mockMvc.perform(multipart("/api/albums")
                        .file(data))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createAlbum_ShouldReturn409_WhenSlugDuplicate() throws Exception {
        when(albumServicePort.createAlbum(any(CreateAlbumRequest.class), any()))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        CreateAlbumRequest request = new CreateAlbumRequest();
        request.setTitle("Album A");
        request.setArtistIds(List.of(UUID.randomUUID()));

        MockMultipartFile data = new MockMultipartFile(
                "data",
                "data",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request));

        mockMvc.perform(multipart("/api/albums")
                        .file(data))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "USER")
    void createAlbum_ShouldReturn403_WhenRoleNotAllowed() throws Exception {
        CreateAlbumRequest request = new CreateAlbumRequest();
        request.setTitle("Album A");
        request.setArtistIds(List.of(UUID.randomUUID()));

        MockMultipartFile data = new MockMultipartFile(
                "data",
                "data",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request));

        mockMvc.perform(multipart("/api/albums")
                        .file(data))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateAlbum_ShouldReturn200_WhenValid() throws Exception {
        UUID id = UUID.randomUUID();
        when(albumServicePort.updateAlbum(eq(id), any(UpdateAlbumRequest.class), any()))
                .thenReturn(buildAlbumResponse(id));

        UpdateAlbumRequest request = new UpdateAlbumRequest();
        request.setTitle("Album A");

        MockMultipartFile data = new MockMultipartFile(
                "data",
                "data",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request));

        mockMvc.perform(multipart("/api/albums/{id}", id)
                        .file(data)
                        .with(req -> { req.setMethod("PUT"); return req; }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateAlbum_ShouldReturn200_WhenCoverMissing() throws Exception {
        UUID id = UUID.randomUUID();
        when(albumServicePort.updateAlbum(eq(id), any(UpdateAlbumRequest.class), any()))
                .thenReturn(buildAlbumResponse(id));

        UpdateAlbumRequest request = new UpdateAlbumRequest();
        request.setTitle("Album A");

        MockMultipartFile data = new MockMultipartFile(
                "data",
                "data",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request));

        mockMvc.perform(multipart("/api/albums/{id}", id)
                        .file(data)
                        .with(req -> { req.setMethod("PUT"); return req; }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateAlbum_ShouldReturn404_WhenNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(albumServicePort.updateAlbum(eq(id), any(UpdateAlbumRequest.class), any()))
                .thenThrow(new AlbumNotFoundException("Album not found"));

        MockMultipartFile data = new MockMultipartFile(
                "data",
                "data",
                MediaType.APPLICATION_JSON_VALUE,
                "{\"title\":\"Album A\"}".getBytes());

        mockMvc.perform(multipart("/api/albums/{id}", id)
                        .file(data)
                        .with(req -> { req.setMethod("PUT"); return req; }))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateAlbum_ShouldReturn404_WhenArtistInvalid() throws Exception {
        UUID id = UUID.randomUUID();
        when(albumServicePort.updateAlbum(eq(id), any(UpdateAlbumRequest.class), any()))
                .thenThrow(new ArtistNotFoundException("Artist not found"));

        MockMultipartFile data = new MockMultipartFile(
                "data",
                "data",
                MediaType.APPLICATION_JSON_VALUE,
                "{\"title\":\"Album A\"}".getBytes());

        mockMvc.perform(multipart("/api/albums/{id}", id)
                        .file(data)
                        .with(req -> { req.setMethod("PUT"); return req; }))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateAlbum_ShouldReturn409_WhenSlugDuplicate() throws Exception {
        UUID id = UUID.randomUUID();
        when(albumServicePort.updateAlbum(eq(id), any(UpdateAlbumRequest.class), any()))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        MockMultipartFile data = new MockMultipartFile(
                "data",
                "data",
                MediaType.APPLICATION_JSON_VALUE,
                "{\"title\":\"Album A\"}".getBytes());

        mockMvc.perform(multipart("/api/albums/{id}", id)
                        .file(data)
                        .with(req -> { req.setMethod("PUT"); return req; }))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateAlbum_ShouldReturn403_WhenRoleNotAllowed() throws Exception {
        UUID id = UUID.randomUUID();
        MockMultipartFile data = new MockMultipartFile(
                "data",
                "data",
                MediaType.APPLICATION_JSON_VALUE,
                "{\"title\":\"Album A\"}".getBytes());

        mockMvc.perform(multipart("/api/albums/{id}", id)
                        .file(data)
                        .with(req -> { req.setMethod("PUT"); return req; }))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAlbumById_ShouldReturn200_WhenExists() throws Exception {
        UUID id = UUID.randomUUID();
        when(albumServicePort.getAlbumById(id)).thenReturn(buildAlbumResponse(id));

        mockMvc.perform(get("/api/albums/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAlbumById_ShouldReturn404_WhenNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(albumServicePort.getAlbumById(id)).thenThrow(new AlbumNotFoundException("Album not found"));

        mockMvc.perform(get("/api/albums/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAlbums_ShouldReturn200_WhenQueryProvided() throws Exception {
        PageResultDto<AlbumResponse> page = PageResultDto.<AlbumResponse>builder()
                .items(List.of(buildAlbumResponse(UUID.randomUUID())))
                .page(0)
                .size(20)
                .totalElements(1)
                .totalPages(1)
                .build();

        when(albumServicePort.getAlbums(any())).thenReturn(page);

        mockMvc.perform(get("/api/albums").param("query", "love"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAlbums_ShouldReturn200_WhenQueryEmpty() throws Exception {
        PageResultDto<AlbumResponse> page = PageResultDto.<AlbumResponse>builder()
                .items(List.of())
                .page(0)
                .size(20)
                .totalElements(0)
                .totalPages(0)
                .build();

        when(albumServicePort.getAlbums(any())).thenReturn(page);

        mockMvc.perform(get("/api/albums"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAlbums_ShouldReturn400_WhenPageNegative() throws Exception {
        when(albumServicePort.getAlbums(any())).thenThrow(new IllegalArgumentException("page must be >= 0"));

        mockMvc.perform(get("/api/albums").param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteAlbum_ShouldReturn200_WhenValid() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(albumServicePort).deleteAlbum(id);

        mockMvc.perform(delete("/api/albums/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteAlbum_ShouldReturn404_WhenNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new AlbumNotFoundException("Album not found")).when(albumServicePort).deleteAlbum(id);

        mockMvc.perform(delete("/api/albums/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteAlbum_ShouldReturn403_WhenRoleNotAllowed() throws Exception {
        mockMvc.perform(delete("/api/albums/{id}", UUID.randomUUID()))
                .andExpect(status().isForbidden());
    }
}
