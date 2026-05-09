package com.example.ondas_be.integration.controller;

import com.example.ondas_be.application.dto.response.SearchResponse;
import com.example.ondas_be.application.dto.response.SearchSuggestionResponse;
import com.example.ondas_be.application.service.port.SearchServicePort;
import com.example.ondas_be.infrastructure.security.JwtUtil;
import com.example.ondas_be.infrastructure.security.SecurityConfig;
import com.example.ondas_be.infrastructure.security.UserDetailsServiceImpl;
import com.example.ondas_be.presentation.advice.GlobalExceptionHandler;
import com.example.ondas_be.presentation.controller.SearchController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SearchController.class)
@AutoConfigureMockMvc
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
class SearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SearchServicePort searchServicePort;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @Test
    @WithMockUser(roles = "USER")
    void search_ShouldReturn200_WhenValidQuery() throws Exception {
        SearchResponse response = SearchResponse.builder()
                .query("love")
                .page(0)
                .size(10)
                .totalSongs(1)
                .totalArtists(1)
                .totalAlbums(1)
                .songs(List.of())
                .artists(List.of())
                .albums(List.of())
                .build();

        when(searchServicePort.search(any())).thenReturn(response);

        mockMvc.perform(get("/api/search").queryParam("query", "love"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.query").value("love"))
                .andExpect(jsonPath("$.data.totalSongs").value(1));
    }

    @Test
    @WithMockUser(roles = "USER")
    void search_ShouldReturn400_WhenQueryMissing() throws Exception {
        when(searchServicePort.search(any())).thenThrow(new IllegalArgumentException("Query is required"));

        mockMvc.perform(get("/api/search"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Query is required"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void search_ShouldReturn400_WhenQueryBlank() throws Exception {
        when(searchServicePort.search(any())).thenThrow(new IllegalArgumentException("Query is required"));

        mockMvc.perform(get("/api/search").queryParam("query", "   "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Query is required"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getSuggestions_ShouldReturn200_WhenAuthenticated() throws Exception {
        SearchSuggestionResponse response = SearchSuggestionResponse.builder()
                .recentSearches(List.of("love"))
                .trendingSearches(List.of("trend"))
                .trendingSongs(List.of())
                .genres(List.of())
                .build();

        when(searchServicePort.getSuggestions(any())).thenReturn(response);

        mockMvc.perform(get("/api/search/suggestions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.recentSearches[0]").value("love"));
    }

    @Test
    void getSuggestions_ShouldReturn401_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/search/suggestions"))
                                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void clearSearchHistory_ShouldReturn200_WhenAuthenticated() throws Exception {
        mockMvc.perform(delete("/api/search/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void clearSearchHistory_ShouldReturn401_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(delete("/api/search/history"))
                                .andExpect(status().isForbidden());
    }
}
