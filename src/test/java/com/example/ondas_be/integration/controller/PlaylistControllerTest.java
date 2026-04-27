package com.example.ondas_be.integration.controller;

import com.example.ondas_be.application.dto.common.PageResultDto;
import com.example.ondas_be.application.dto.request.PlaylistFilterRequest;
import com.example.ondas_be.application.dto.response.PlaylistResponse;
import com.example.ondas_be.application.exception.PlaylistNotFoundException;
import com.example.ondas_be.application.service.port.PlaylistServicePort;
import com.example.ondas_be.presentation.advice.GlobalExceptionHandler;
import com.example.ondas_be.presentation.controller.PlaylistController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PlaylistController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class PlaylistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlaylistServicePort playlistServicePort;

    @Test
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

        when(playlistServicePort.getPlaylists(isNull(), any(PlaylistFilterRequest.class))).thenReturn(page);

        mockMvc.perform(get("/api/playlists"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items[0].name").value("Public Playlist"));
    }

    @Test
    void getPlaylistById_ShouldReturn404_WhenNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(playlistServicePort.getPlaylistById(null, id))
                .thenThrow(new PlaylistNotFoundException("Playlist not found with id: " + id));

        mockMvc.perform(get("/api/playlists/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }
}
