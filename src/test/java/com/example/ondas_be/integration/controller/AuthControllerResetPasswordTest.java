package com.example.ondas_be.integration.controller;

import com.example.ondas_be.application.dto.request.ResetPasswordRequest;
import com.example.ondas_be.application.exception.InvalidTokenException;
import com.example.ondas_be.application.service.port.AuthServicePort;
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

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AuthControllerResetPasswordTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthServicePort authServicePort;

    @Test
    void resetPassword_ShouldReturn200_WhenRequestValid() throws Exception {
        doNothing().when(authServicePort).resetPassword(any(ResetPasswordRequest.class));

        ResetPasswordRequest request = new ResetPasswordRequest("user@example.com", "123456", "new-password-123");

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("OK"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void resetPassword_ShouldReturn401_WhenOtpInvalid() throws Exception {
        doThrow(new InvalidTokenException("Invalid or expired OTP"))
                .when(authServicePort).resetPassword(any(ResetPasswordRequest.class));

        ResetPasswordRequest request = new ResetPasswordRequest("user@example.com", "123456", "new-password-123");

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid or expired OTP"));
    }

    @Test
    void resetPassword_ShouldReturn400_WhenRequestInvalid() throws Exception {
        String invalidRequest = """
                {
                  \"email\": \"user@example.com\",
                  \"otp\": \"123\",
                  \"newPassword\": \"123\"
                }
                """;

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message", containsString("OTP must be 6 digits")));
    }
}
