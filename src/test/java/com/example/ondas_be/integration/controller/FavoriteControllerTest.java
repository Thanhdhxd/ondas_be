package com.example.ondas_be.integration.controller;

import com.example.ondas_be.application.dto.common.PageResultDto;
import com.example.ondas_be.application.dto.response.FavoriteSongResponse;
import com.example.ondas_be.application.exception.FavoriteAlreadyExistsException;
import com.example.ondas_be.application.exception.FavoriteNotFoundException;
import com.example.ondas_be.application.exception.SongNotFoundException;
import com.example.ondas_be.application.service.port.FavoriteServicePort;
import com.example.ondas_be.infrastructure.security.JwtUtil;
import com.example.ondas_be.infrastructure.security.SecurityConfig;
import com.example.ondas_be.infrastructure.security.UserDetailsServiceImpl;
import com.example.ondas_be.presentation.advice.GlobalExceptionHandler;
import com.example.ondas_be.presentation.controller.FavoriteController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = FavoriteController.class)
@AutoConfigureMockMvc
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
class FavoriteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FavoriteServicePort favoriteServicePort;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @Test
    @WithMockUser(roles = "USER")
    void addFavorite_ShouldReturn200_WhenValid() throws Exception {
        UUID songId = UUID.randomUUID();
        doNothing().when(favoriteServicePort).addFavorite(any(), eq(songId));

        mockMvc.perform(post("/api/favorites/{songId}", songId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "USER")
    void addFavorite_ShouldReturn409_WhenAlreadyExists() throws Exception {
        UUID songId = UUID.randomUUID();
        doThrow(new FavoriteAlreadyExistsException("Favorite exists"))
                .when(favoriteServicePort).addFavorite(any(), eq(songId));

        mockMvc.perform(post("/api/favorites/{songId}", songId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "USER")
    void addFavorite_ShouldReturn404_WhenSongNotFound() throws Exception {
        UUID songId = UUID.randomUUID();
        doThrow(new SongNotFoundException("Song not found"))
                .when(favoriteServicePort).addFavorite(any(), eq(songId));

        mockMvc.perform(post("/api/favorites/{songId}", songId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void addFavorite_ShouldReturn401_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(post("/api/favorites/{songId}", UUID.randomUUID()))
                                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void removeFavorite_ShouldReturn200_WhenValid() throws Exception {
        UUID songId = UUID.randomUUID();
        doNothing().when(favoriteServicePort).removeFavorite(any(), eq(songId));

        mockMvc.perform(delete("/api/favorites/{songId}", songId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "USER")
    void removeFavorite_ShouldReturn404_WhenNotInFavorites() throws Exception {
        UUID songId = UUID.randomUUID();
        doThrow(new FavoriteNotFoundException("Favorite not found"))
                .when(favoriteServicePort).removeFavorite(any(), eq(songId));

        mockMvc.perform(delete("/api/favorites/{songId}", songId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void removeFavorite_ShouldReturn401_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(delete("/api/favorites/{songId}", UUID.randomUUID()))
                                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void isFavorite_ShouldReturn200_WhenExists() throws Exception {
        UUID songId = UUID.randomUUID();
        when(favoriteServicePort.isFavorite(any(), eq(songId))).thenReturn(true);

        mockMvc.perform(get("/api/favorites/{songId}/status", songId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @WithMockUser(roles = "USER")
    void isFavorite_ShouldReturn404_WhenSongNotFound() throws Exception {
        UUID songId = UUID.randomUUID();
        when(favoriteServicePort.isFavorite(any(), eq(songId)))
                .thenThrow(new SongNotFoundException("Song not found"));

        mockMvc.perform(get("/api/favorites/{songId}/status", songId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getFavorites_ShouldReturn200_WhenValid() throws Exception {
        FavoriteSongResponse item = FavoriteSongResponse.builder()
                .songId(UUID.randomUUID())
                .title("Song A")
                .build();

        PageResultDto<FavoriteSongResponse> page = PageResultDto.<FavoriteSongResponse>builder()
                .items(List.of(item))
                .page(0)
                .size(20)
                .totalElements(1)
                .totalPages(1)
                .build();

        when(favoriteServicePort.getFavorites(any(), eq(0), eq(20))).thenReturn(page);

        mockMvc.perform(get("/api/favorites"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items[0].title").value("Song A"));
    }

    @Test
    void getFavorites_ShouldReturn401_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/favorites"))
                                .andExpect(status().isForbidden());
    }
}
