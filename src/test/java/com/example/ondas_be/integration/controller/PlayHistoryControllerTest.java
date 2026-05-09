package com.example.ondas_be.integration.controller;

import com.example.ondas_be.application.dto.common.PageResultDto;
import com.example.ondas_be.application.dto.request.RecordPlayRequest;
import com.example.ondas_be.application.dto.response.PlayHistoryResponse;
import com.example.ondas_be.application.dto.response.PlayHistorySongInfo;
import com.example.ondas_be.application.exception.PlayHistoryNotFoundException;
import com.example.ondas_be.application.exception.SongNotFoundException;
import com.example.ondas_be.application.service.port.PlayHistoryServicePort;
import com.example.ondas_be.infrastructure.security.JwtUtil;
import com.example.ondas_be.infrastructure.security.SecurityConfig;
import com.example.ondas_be.infrastructure.security.UserDetailsServiceImpl;
import com.example.ondas_be.presentation.advice.GlobalExceptionHandler;
import com.example.ondas_be.presentation.controller.PlayHistoryController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
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

@WebMvcTest(controllers = PlayHistoryController.class)
@AutoConfigureMockMvc
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
class PlayHistoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PlayHistoryServicePort playHistoryServicePort;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @Test
    @WithMockUser(roles = "USER")
    void getMyHistory_ShouldReturn200_WhenValid() throws Exception {
        PlayHistoryResponse item = PlayHistoryResponse.builder()
                .id(1L)
                .song(PlayHistorySongInfo.builder()
                        .id(UUID.randomUUID())
                        .title("Song A")
                        .build())
                .playedAt(LocalDateTime.now())
                .source("home")
                .build();

        PageResultDto<PlayHistoryResponse> page = PageResultDto.<PlayHistoryResponse>builder()
                .items(List.of(item))
                .page(0)
                .size(20)
                .totalElements(1)
                .totalPages(1)
                .build();

        when(playHistoryServicePort.getMyHistory(any(), eq(0), eq(20))).thenReturn(page);

        mockMvc.perform(get("/api/play-history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getMyHistory_ShouldReturn400_WhenPageNegative() throws Exception {
        when(playHistoryServicePort.getMyHistory(any(), eq(-1), eq(20)))
                .thenThrow(new IllegalArgumentException("page must be >= 0"));

        mockMvc.perform(get("/api/play-history").param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void getMyHistory_ShouldReturn401_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/play-history"))
                                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void clearMyHistory_ShouldReturn200_WhenValid() throws Exception {
        doNothing().when(playHistoryServicePort).clearMyHistory(any());

        mockMvc.perform(delete("/api/play-history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void clearMyHistory_ShouldReturn401_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(delete("/api/play-history"))
                                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteHistoryEntry_ShouldReturn200_WhenValid() throws Exception {
        doNothing().when(playHistoryServicePort).deleteHistoryEntry(any(), eq(1L));

        mockMvc.perform(delete("/api/play-history/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteHistoryEntry_ShouldReturn404_WhenNotFound() throws Exception {
        doThrow(new PlayHistoryNotFoundException("Entry not found"))
                .when(playHistoryServicePort).deleteHistoryEntry(any(), eq(1L));

        mockMvc.perform(delete("/api/play-history/{id}", 1))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void deleteHistoryEntry_ShouldReturn401_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(delete("/api/play-history/{id}", 1))
                                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void recordPlay_ShouldReturn200_WhenValid() throws Exception {
        doNothing().when(playHistoryServicePort).recordPlay(any(), any(), any());

        RecordPlayRequest request = new RecordPlayRequest();
        request.setSongId(UUID.randomUUID());
        request.setSource("home");

        mockMvc.perform(post("/api/play-history")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "USER")
    void recordPlay_ShouldReturn400_WhenSourceInvalid() throws Exception {
        RecordPlayRequest request = new RecordPlayRequest();
        request.setSongId(UUID.randomUUID());
        request.setSource("invalid");

        mockMvc.perform(post("/api/play-history")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "USER")
    void recordPlay_ShouldReturn400_WhenSongIdMissing() throws Exception {
        RecordPlayRequest request = new RecordPlayRequest();
        request.setSource("home");

        mockMvc.perform(post("/api/play-history")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "USER")
    void recordPlay_ShouldReturn404_WhenSongNotFound() throws Exception {
        doThrow(new SongNotFoundException("Song not found"))
                .when(playHistoryServicePort).recordPlay(any(), any(), any());

        RecordPlayRequest request = new RecordPlayRequest();
        request.setSongId(UUID.randomUUID());
        request.setSource("home");

        mockMvc.perform(post("/api/play-history")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void recordPlay_ShouldReturn401_WhenNotAuthenticated() throws Exception {
        RecordPlayRequest request = new RecordPlayRequest();
        request.setSongId(UUID.randomUUID());
        request.setSource("home");

        mockMvc.perform(post("/api/play-history")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isForbidden());
    }
}
