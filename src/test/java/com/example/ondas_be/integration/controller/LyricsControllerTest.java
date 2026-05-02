package com.example.ondas_be.integration.controller;

import com.example.ondas_be.application.dto.request.UpdateStaticLyricsRequest;
import com.example.ondas_be.application.dto.response.LyricsResponse;
import com.example.ondas_be.application.service.port.LyricsServicePort;
import com.example.ondas_be.infrastructure.security.JwtUtil;
import com.example.ondas_be.presentation.controller.LyricsController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LyricsController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for simple endpoint testing
class LyricsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LyricsServicePort lyricsServicePort;

    @MockBean
    private JwtUtil jwtUtil; // Mocked because of security config

    private UUID songId;
    private UUID lyricsId;

    @BeforeEach
    void setUp() {
        songId = UUID.randomUUID();
        lyricsId = UUID.randomUUID();
    }

    @Test
    void getLyrics_ShouldReturn200AndLyricsResponse() throws Exception {
        LyricsResponse response = LyricsResponse.builder()
                .id(lyricsId)
                .songId(songId)
                .plainText("Test Lyrics")
                .hasSynced(false)
                .build();

        when(lyricsServicePort.getLyricsBySongId(songId)).thenReturn(response);

        mockMvc.perform(get("/api/songs/{songId}/lyrics", songId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.plainText").value("Test Lyrics"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateStaticLyrics_ShouldReturn200AndUpdatedLyrics() throws Exception {
        UpdateStaticLyricsRequest request = new UpdateStaticLyricsRequest();
        request.setPlainText("Updated text");

        LyricsResponse response = LyricsResponse.builder()
                .id(lyricsId)
                .songId(songId)
                .plainText("Updated text")
                .hasSynced(false)
                .build();

        when(lyricsServicePort.updateStaticLyrics(eq(songId), any(UpdateStaticLyricsRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/songs/{songId}/lyrics/static", songId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.plainText").value("Updated text"));
    }

    @Test
    void updateStaticLyrics_ShouldReturn400_WhenPlainTextIsBlank() throws Exception {
        UpdateStaticLyricsRequest request = new UpdateStaticLyricsRequest();
        request.setPlainText(""); // Invalid

        mockMvc.perform(put("/api/songs/{songId}/lyrics/static", songId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isBadRequest()); // Validator kicks in
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteLyrics_ShouldReturn200() throws Exception {
        doNothing().when(lyricsServicePort).deleteLyrics(songId);

        mockMvc.perform(delete("/api/songs/{songId}/lyrics", songId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        
        verify(lyricsServicePort).deleteLyrics(songId);
    }
}
