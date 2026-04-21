package com.example.ondas_be.integration.controller;

import com.example.ondas_be.application.dto.request.RegisterRequest;
import com.example.ondas_be.application.dto.response.AuthResponse;
import com.example.ondas_be.application.dto.response.UserSummaryResponse;
import com.example.ondas_be.application.exception.EmailAlreadyExistsException;
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
class AuthControllerRegisterTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthServicePort authServicePort;

    @Test
    void register_ShouldReturn201_WhenRequestValid() throws Exception {
        AuthResponse authResponse = new AuthResponse(
                "jwt-token",
                "refresh-token",
                new UserSummaryResponse(
                        UUID.randomUUID(),
                        "user@example.com",
                        "Test User",
                        Role.USER
                )
        );

        when(authServicePort.register(any(RegisterRequest.class))).thenReturn(authResponse);

        RegisterRequest request = new RegisterRequest("user@example.com", "12345678", "Test User");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("OK"))
                .andExpect(jsonPath("$.data.accessToken").value("jwt-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.data.user.email").value("user@example.com"));
    }

    @Test
    void register_ShouldReturn409_WhenEmailExists() throws Exception {
        when(authServicePort.register(any(RegisterRequest.class)))
                .thenThrow(new EmailAlreadyExistsException("Email already exists"));

        RegisterRequest request = new RegisterRequest("user@example.com", "12345678", "Test User");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Email already exists"));
    }

    @Test
    void register_ShouldReturn400_WhenRequestInvalid() throws Exception {
        String invalidRequest = """
                {
                  \"email\": \"\",
                  \"password\": \"123\",
                  \"displayName\": \"\"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message", containsString("Email is required")));
    }
}
