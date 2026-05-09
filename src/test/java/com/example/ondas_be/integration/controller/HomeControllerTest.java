package com.example.ondas_be.integration.controller;

import com.example.ondas_be.application.dto.response.HomeResponse;
import com.example.ondas_be.application.service.port.HomeServicePort;
import com.example.ondas_be.infrastructure.security.JwtUtil;
import com.example.ondas_be.infrastructure.security.SecurityConfig;
import com.example.ondas_be.infrastructure.security.UserDetailsServiceImpl;
import com.example.ondas_be.presentation.advice.GlobalExceptionHandler;
import com.example.ondas_be.presentation.controller.HomeController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = HomeController.class)
@AutoConfigureMockMvc
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HomeServicePort homeServicePort;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @Test
    @WithMockUser(roles = "USER")
    void getHome_ShouldReturn200_WhenDefaults() throws Exception {
        HomeResponse response = HomeResponse.builder()
                .trendingSongs(List.of())
                .featuredArtists(List.of())
                .newReleases(List.of())
                .build();

        when(homeServicePort.getHome(eq(10), eq(10), eq(10))).thenReturn(response);

        mockMvc.perform(get("/api/home"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getHome_ShouldReturn200_WhenLimitsProvided() throws Exception {
        HomeResponse response = HomeResponse.builder()
                .trendingSongs(List.of())
                .featuredArtists(List.of())
                .newReleases(List.of())
                .build();

        when(homeServicePort.getHome(eq(5), eq(6), eq(7))).thenReturn(response);

        mockMvc.perform(get("/api/home")
                        .param("trendingLimit", "5")
                        .param("artistLimit", "6")
                        .param("albumLimit", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getHome_ShouldReturn400_WhenLimitNegative() throws Exception {
        when(homeServicePort.getHome(eq(-1), eq(10), eq(10)))
                .thenThrow(new IllegalArgumentException("limit must be >= 0"));

        mockMvc.perform(get("/api/home")
                        .param("trendingLimit", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
