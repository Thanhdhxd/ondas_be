package com.example.ondas_be.integration.controller;

import com.example.ondas_be.application.dto.request.LoginRequest;
import com.example.ondas_be.application.dto.response.AuthResponse;
import com.example.ondas_be.application.dto.response.UserSummaryResponse;
import com.example.ondas_be.application.exception.InvalidCredentialsException;
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
class AuthControllerLoginTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthServicePort authServicePort;

    @Test
    void login_ShouldReturn200_WhenCredentialsValid() throws Exception {
        AuthResponse authResponse = new AuthResponse(
                "access-token",
                "refresh-token",
                new UserSummaryResponse(
                        UUID.randomUUID(),
                        "user@example.com",
                        "Test User",
                        Role.USER
                )
        );

        when(authServicePort.login(any(LoginRequest.class))).thenReturn(authResponse);

        LoginRequest request = new LoginRequest("user@example.com", "12345678");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("OK"))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"));
    }

    @Test
    void login_ShouldReturn401_WhenCredentialsInvalid() throws Exception {
        when(authServicePort.login(any(LoginRequest.class)))
                .thenThrow(new InvalidCredentialsException("Invalid credentials"));

        LoginRequest request = new LoginRequest("user@example.com", "wrong-password");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }

    @Test
    void login_ShouldReturn400_WhenRequestInvalid() throws Exception {
        String invalidRequest = """
                {
                  \"email\": \"\",
                  \"password\": \"\"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message", containsString("Email is required")));
    }
}
