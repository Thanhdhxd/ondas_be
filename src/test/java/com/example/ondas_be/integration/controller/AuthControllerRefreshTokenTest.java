package com.example.ondas_be.integration.controller;

import com.example.ondas_be.application.dto.request.RefreshTokenRequest;
import com.example.ondas_be.application.dto.response.AuthResponse;
import com.example.ondas_be.application.dto.response.UserSummaryResponse;
import com.example.ondas_be.application.exception.InvalidTokenException;
import com.example.ondas_be.application.service.port.AuthServicePort;
import com.example.ondas_be.domain.entity.Role;
import com.example.ondas_be.presentation.advice.GlobalExceptionHandler;
import com.example.ondas_be.presentation.controller.AuthController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AuthControllerRefreshTokenTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthServicePort authServicePort;

    @Test
    void refreshToken_ShouldReturn200_WhenRequestValid() throws Exception {
        AuthResponse authResponse = new AuthResponse(
                "new-access-token",
                "new-refresh-token",
                new UserSummaryResponse(
                        UUID.randomUUID(),
                        "user@example.com",
                        "Test User",
                        Role.USER
                )
        );

        when(authServicePort.refreshToken(any(RefreshTokenRequest.class))).thenReturn(authResponse);

        RefreshTokenRequest request = new RefreshTokenRequest("valid-refresh-token");

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("OK"))
                .andExpect(jsonPath("$.data.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("new-refresh-token"));
    }

    @Test
    void refreshToken_ShouldReturn401_WhenTokenInvalid() throws Exception {
        when(authServicePort.refreshToken(any(RefreshTokenRequest.class)))
                .thenThrow(new InvalidTokenException("Invalid refresh token"));

        RefreshTokenRequest request = new RefreshTokenRequest("invalid-token");

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid refresh token"));
    }

    @Test
    void refreshToken_ShouldReturn400_WhenRequestInvalid() throws Exception {
        String invalidRequest = """
                {
                  \"refreshToken\": \"\"
                }
                """;

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message", containsString("Refresh token is required")));
    }
}
