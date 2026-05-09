package com.example.ondas_be.integration.controller;

import com.example.ondas_be.application.dto.common.PageResultDto;
import com.example.ondas_be.application.dto.request.CreateArtistRequest;
import com.example.ondas_be.application.dto.request.UpdateArtistRequest;
import com.example.ondas_be.application.dto.response.ArtistResponse;
import com.example.ondas_be.application.exception.ArtistNotFoundException;
import com.example.ondas_be.application.service.port.ArtistServicePort;
import com.example.ondas_be.infrastructure.security.JwtUtil;
import com.example.ondas_be.infrastructure.security.SecurityConfig;
import com.example.ondas_be.infrastructure.security.UserDetailsServiceImpl;
import com.example.ondas_be.presentation.advice.GlobalExceptionHandler;
import com.example.ondas_be.presentation.controller.ArtistController;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ArtistController.class)
@AutoConfigureMockMvc
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
class ArtistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ArtistServicePort artistServicePort;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    private ArtistResponse buildArtistResponse(UUID id) {
        return ArtistResponse.builder()
                .id(id)
                .name("Artist A")
                .slug("artist-a")
                .bio("bio")
                .avatarUrl("avatar-url")
                .country("VN")
                .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createArtist_ShouldReturn201_WhenValid() throws Exception {
        UUID id = UUID.randomUUID();
        when(artistServicePort.createArtist(any(CreateArtistRequest.class), any()))
                .thenReturn(buildArtistResponse(id));

        CreateArtistRequest request = new CreateArtistRequest();
        request.setName("Artist A");

        MockMultipartFile data = new MockMultipartFile(
                "data",
                "data",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request));
        MockMultipartFile avatar = new MockMultipartFile(
                "avatar",
                "avatar.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "img".getBytes());

        mockMvc.perform(multipart("/api/artists")
                        .file(data)
                        .file(avatar))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Artist A"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createArtist_ShouldReturn201_WhenAvatarMissing() throws Exception {
        UUID id = UUID.randomUUID();
        when(artistServicePort.createArtist(any(CreateArtistRequest.class), any()))
                .thenReturn(buildArtistResponse(id));

        CreateArtistRequest request = new CreateArtistRequest();
        request.setName("Artist A");

        MockMultipartFile data = new MockMultipartFile(
                "data",
                "data",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request));

        mockMvc.perform(multipart("/api/artists")
                        .file(data))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createArtist_ShouldReturn400_WhenNameBlank() throws Exception {
        CreateArtistRequest request = new CreateArtistRequest();
        request.setName(" ");

        MockMultipartFile data = new MockMultipartFile(
                "data",
                "data",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request));

        mockMvc.perform(multipart("/api/artists")
                        .file(data))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createArtist_ShouldReturn409_WhenSlugDuplicate() throws Exception {
        when(artistServicePort.createArtist(any(CreateArtistRequest.class), any()))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        CreateArtistRequest request = new CreateArtistRequest();
        request.setName("Artist A");

        MockMultipartFile data = new MockMultipartFile(
                "data",
                "data",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request));

        mockMvc.perform(multipart("/api/artists")
                        .file(data))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "USER")
    void createArtist_ShouldReturn403_WhenRoleNotAllowed() throws Exception {
        CreateArtistRequest request = new CreateArtistRequest();
        request.setName("Artist A");

        MockMultipartFile data = new MockMultipartFile(
                "data",
                "data",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request));

        mockMvc.perform(multipart("/api/artists")
                        .file(data))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateArtist_ShouldReturn200_WhenValid() throws Exception {
        UUID id = UUID.randomUUID();
        when(artistServicePort.updateArtist(eq(id), any(UpdateArtistRequest.class), any()))
                .thenReturn(buildArtistResponse(id));

        UpdateArtistRequest request = new UpdateArtistRequest();
        request.setName("Artist A");

        MockMultipartFile data = new MockMultipartFile(
                "data",
                "data",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request));

        mockMvc.perform(multipart("/api/artists/{id}", id)
                        .file(data)
                        .with(req -> { req.setMethod("PUT"); return req; }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateArtist_ShouldReturn200_WhenAvatarMissing() throws Exception {
        UUID id = UUID.randomUUID();
        when(artistServicePort.updateArtist(eq(id), any(UpdateArtistRequest.class), any()))
                .thenReturn(buildArtistResponse(id));

        UpdateArtistRequest request = new UpdateArtistRequest();
        request.setName("Artist A");

        MockMultipartFile data = new MockMultipartFile(
                "data",
                "data",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request));

        mockMvc.perform(multipart("/api/artists/{id}", id)
                        .file(data)
                        .with(req -> { req.setMethod("PUT"); return req; }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateArtist_ShouldReturn404_WhenNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(artistServicePort.updateArtist(eq(id), any(UpdateArtistRequest.class), any()))
                .thenThrow(new ArtistNotFoundException("Artist not found"));

        MockMultipartFile data = new MockMultipartFile(
                "data",
                "data",
                MediaType.APPLICATION_JSON_VALUE,
                "{\"name\":\"Artist A\"}".getBytes());

        mockMvc.perform(multipart("/api/artists/{id}", id)
                        .file(data)
                        .with(req -> { req.setMethod("PUT"); return req; }))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateArtist_ShouldReturn409_WhenSlugDuplicate() throws Exception {
        UUID id = UUID.randomUUID();
        when(artistServicePort.updateArtist(eq(id), any(UpdateArtistRequest.class), any()))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        MockMultipartFile data = new MockMultipartFile(
                "data",
                "data",
                MediaType.APPLICATION_JSON_VALUE,
                "{\"name\":\"Artist A\"}".getBytes());

        mockMvc.perform(multipart("/api/artists/{id}", id)
                        .file(data)
                        .with(req -> { req.setMethod("PUT"); return req; }))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateArtist_ShouldReturn403_WhenRoleNotAllowed() throws Exception {
        UUID id = UUID.randomUUID();
        MockMultipartFile data = new MockMultipartFile(
                "data",
                "data",
                MediaType.APPLICATION_JSON_VALUE,
                "{\"name\":\"Artist A\"}".getBytes());

        mockMvc.perform(multipart("/api/artists/{id}", id)
                        .file(data)
                        .with(req -> { req.setMethod("PUT"); return req; }))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getArtistById_ShouldReturn200_WhenExists() throws Exception {
        UUID id = UUID.randomUUID();
        when(artistServicePort.getArtistById(id)).thenReturn(buildArtistResponse(id));

        mockMvc.perform(get("/api/artists/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getArtistById_ShouldReturn404_WhenNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(artistServicePort.getArtistById(id)).thenThrow(new ArtistNotFoundException("Artist not found"));

        mockMvc.perform(get("/api/artists/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getArtists_ShouldReturn200_WhenQueryProvided() throws Exception {
        PageResultDto<ArtistResponse> page = PageResultDto.<ArtistResponse>builder()
                .items(List.of(buildArtistResponse(UUID.randomUUID())))
                .page(0)
                .size(20)
                .totalElements(1)
                .totalPages(1)
                .build();

        when(artistServicePort.getArtists(any())).thenReturn(page);

        mockMvc.perform(get("/api/artists").param("query", "love"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getArtists_ShouldReturn400_WhenPageNegative() throws Exception {
        when(artistServicePort.getArtists(any()))
                .thenThrow(new IllegalArgumentException("page must be >= 0"));

        mockMvc.perform(get("/api/artists").param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteArtist_ShouldReturn200_WhenValid() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(artistServicePort).deleteArtist(id);

        mockMvc.perform(delete("/api/artists/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteArtist_ShouldReturn404_WhenNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new ArtistNotFoundException("Artist not found")).when(artistServicePort).deleteArtist(id);

        mockMvc.perform(delete("/api/artists/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteArtist_ShouldReturn403_WhenRoleNotAllowed() throws Exception {
        mockMvc.perform(delete("/api/artists/{id}", UUID.randomUUID()))
                .andExpect(status().isForbidden());
    }
}
