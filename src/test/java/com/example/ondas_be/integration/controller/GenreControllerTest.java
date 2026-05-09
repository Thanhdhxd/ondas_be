package com.example.ondas_be.integration.controller;

import com.example.ondas_be.application.dto.common.PageResultDto;
import com.example.ondas_be.application.dto.request.CreateGenreRequest;
import com.example.ondas_be.application.dto.request.UpdateGenreRequest;
import com.example.ondas_be.application.dto.response.GenreResponse;
import com.example.ondas_be.application.exception.GenreNotFoundException;
import com.example.ondas_be.application.service.port.GenreServicePort;
import com.example.ondas_be.infrastructure.security.JwtUtil;
import com.example.ondas_be.infrastructure.security.SecurityConfig;
import com.example.ondas_be.infrastructure.security.UserDetailsServiceImpl;
import com.example.ondas_be.presentation.advice.GlobalExceptionHandler;
import com.example.ondas_be.presentation.controller.GenreController;
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

@WebMvcTest(controllers = GenreController.class)
@AutoConfigureMockMvc
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
class GenreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GenreServicePort genreServicePort;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    private GenreResponse buildGenreResponse(Long id) {
        return GenreResponse.builder()
                .id(id)
                .name("Pop")
                .slug("pop")
                .description("desc")
                .coverUrl("cover-url")
                .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createGenre_ShouldReturn201_WhenValid() throws Exception {
        when(genreServicePort.createGenre(any(CreateGenreRequest.class), any()))
                .thenReturn(buildGenreResponse(1L));

        CreateGenreRequest request = new CreateGenreRequest();
        request.setName("Pop");

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

        mockMvc.perform(multipart("/api/genres")
                        .file(data)
                        .file(cover))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Pop"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createGenre_ShouldReturn201_WhenCoverMissing() throws Exception {
        when(genreServicePort.createGenre(any(CreateGenreRequest.class), any()))
                .thenReturn(buildGenreResponse(1L));

        CreateGenreRequest request = new CreateGenreRequest();
        request.setName("Pop");

        MockMultipartFile data = new MockMultipartFile(
                "data",
                "data",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request));

        mockMvc.perform(multipart("/api/genres")
                        .file(data))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createGenre_ShouldReturn400_WhenNameBlank() throws Exception {
        CreateGenreRequest request = new CreateGenreRequest();
        request.setName(" ");

        MockMultipartFile data = new MockMultipartFile(
                "data",
                "data",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request));

        mockMvc.perform(multipart("/api/genres")
                        .file(data))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createGenre_ShouldReturn409_WhenSlugDuplicate() throws Exception {
        when(genreServicePort.createGenre(any(CreateGenreRequest.class), any()))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        CreateGenreRequest request = new CreateGenreRequest();
        request.setName("Pop");

        MockMultipartFile data = new MockMultipartFile(
                "data",
                "data",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request));

        mockMvc.perform(multipart("/api/genres")
                        .file(data))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "USER")
    void createGenre_ShouldReturn403_WhenRoleNotAllowed() throws Exception {
        CreateGenreRequest request = new CreateGenreRequest();
        request.setName("Pop");

        MockMultipartFile data = new MockMultipartFile(
                "data",
                "data",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request));

        mockMvc.perform(multipart("/api/genres")
                        .file(data))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateGenre_ShouldReturn200_WhenValid() throws Exception {
        when(genreServicePort.updateGenre(eq(1L), any(UpdateGenreRequest.class), any()))
                .thenReturn(buildGenreResponse(1L));

        UpdateGenreRequest request = new UpdateGenreRequest();
        request.setName("Pop");

        MockMultipartFile data = new MockMultipartFile(
                "data",
                "data",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request));

        mockMvc.perform(multipart("/api/genres/{id}", 1)
                        .file(data)
                        .with(req -> { req.setMethod("PUT"); return req; }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateGenre_ShouldReturn404_WhenNotFound() throws Exception {
        when(genreServicePort.updateGenre(eq(1L), any(UpdateGenreRequest.class), any()))
                .thenThrow(new GenreNotFoundException("Genre not found"));

        MockMultipartFile data = new MockMultipartFile(
                "data",
                "data",
                MediaType.APPLICATION_JSON_VALUE,
                "{\"name\":\"Pop\"}".getBytes());

        mockMvc.perform(multipart("/api/genres/{id}", 1)
                        .file(data)
                        .with(req -> { req.setMethod("PUT"); return req; }))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateGenre_ShouldReturn409_WhenSlugDuplicate() throws Exception {
        when(genreServicePort.updateGenre(eq(1L), any(UpdateGenreRequest.class), any()))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        MockMultipartFile data = new MockMultipartFile(
                "data",
                "data",
                MediaType.APPLICATION_JSON_VALUE,
                "{\"name\":\"Pop\"}".getBytes());

        mockMvc.perform(multipart("/api/genres/{id}", 1)
                        .file(data)
                        .with(req -> { req.setMethod("PUT"); return req; }))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateGenre_ShouldReturn403_WhenRoleNotAllowed() throws Exception {
        MockMultipartFile data = new MockMultipartFile(
                "data",
                "data",
                MediaType.APPLICATION_JSON_VALUE,
                "{\"name\":\"Pop\"}".getBytes());

        mockMvc.perform(multipart("/api/genres/{id}", 1)
                        .file(data)
                        .with(req -> { req.setMethod("PUT"); return req; }))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getGenreById_ShouldReturn200_WhenExists() throws Exception {
        when(genreServicePort.getGenreById(1L)).thenReturn(buildGenreResponse(1L));

        mockMvc.perform(get("/api/genres/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getGenreById_ShouldReturn404_WhenNotFound() throws Exception {
        when(genreServicePort.getGenreById(1L)).thenThrow(new GenreNotFoundException("Genre not found"));

        mockMvc.perform(get("/api/genres/{id}", 1))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllGenres_ShouldReturn200_WhenValid() throws Exception {
        when(genreServicePort.getAllGenres()).thenReturn(List.of(buildGenreResponse(1L)));

        mockMvc.perform(get("/api/genres"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "USER")
    void searchGenres_ShouldReturn200_WhenQueryValid() throws Exception {
        PageResultDto<GenreResponse> page = PageResultDto.<GenreResponse>builder()
                .items(List.of(buildGenreResponse(1L)))
                .page(0)
                .size(20)
                .totalElements(1)
                .totalPages(1)
                .build();

        when(genreServicePort.searchGenresByName(eq("pop"), eq("contains"), eq(0), eq(20)))
                .thenReturn(page);

        mockMvc.perform(get("/api/genres/search")
                        .param("query", "pop")
                        .param("mode", "contains"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "USER")
    void searchGenres_ShouldReturn400_WhenModeInvalid() throws Exception {
        when(genreServicePort.searchGenresByName(eq("pop"), eq("invalid"), eq(0), eq(20)))
                .thenThrow(new IllegalArgumentException("mode invalid"));

        mockMvc.perform(get("/api/genres/search")
                        .param("query", "pop")
                        .param("mode", "invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "USER")
    void searchGenres_ShouldReturn400_WhenQueryMissing() throws Exception {
        mockMvc.perform(get("/api/genres/search"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteGenre_ShouldReturn200_WhenValid() throws Exception {
        doNothing().when(genreServicePort).deleteGenre(1L);

        mockMvc.perform(delete("/api/genres/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteGenre_ShouldReturn404_WhenNotFound() throws Exception {
        doThrow(new GenreNotFoundException("Genre not found")).when(genreServicePort).deleteGenre(1L);

        mockMvc.perform(delete("/api/genres/{id}", 1))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteGenre_ShouldReturn403_WhenRoleNotAllowed() throws Exception {
        mockMvc.perform(delete("/api/genres/{id}", 1))
                .andExpect(status().isForbidden());
    }
}
