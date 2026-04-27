package com.example.ondas_be.integration.controller;

import com.example.ondas_be.application.dto.response.SearchResponse;
import com.example.ondas_be.application.service.port.SearchServicePort;
import com.example.ondas_be.presentation.advice.GlobalExceptionHandler;
import com.example.ondas_be.presentation.controller.SearchController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SearchController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class SearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SearchServicePort searchServicePort;

    @Test
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
    void search_ShouldReturn400_WhenQueryMissing() throws Exception {
        when(searchServicePort.search(any())).thenThrow(new IllegalArgumentException("Query is required"));

        mockMvc.perform(get("/api/search"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Query is required"));
    }
}
