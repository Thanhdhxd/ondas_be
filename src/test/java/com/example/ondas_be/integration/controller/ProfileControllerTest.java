package com.example.ondas_be.integration.controller;

import com.example.ondas_be.application.dto.request.ChangePasswordRequest;
import com.example.ondas_be.application.dto.request.UpdateProfileRequest;
import com.example.ondas_be.application.dto.response.UserProfileResponse;
import com.example.ondas_be.application.exception.InvalidCurrentPasswordException;
import com.example.ondas_be.application.service.port.ProfileServicePort;
import com.example.ondas_be.domain.entity.Role;
import com.example.ondas_be.infrastructure.security.JwtUtil;
import com.example.ondas_be.infrastructure.security.SecurityConfig;
import com.example.ondas_be.infrastructure.security.UserDetailsServiceImpl;
import com.example.ondas_be.presentation.advice.GlobalExceptionHandler;
import com.example.ondas_be.presentation.controller.ProfileController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ProfileController.class)
@AutoConfigureMockMvc
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProfileServicePort profileServicePort;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    private UserProfileResponse buildProfileResponse() {
        return new UserProfileResponse(
                UUID.randomUUID(),
                "user@example.com",
                "User",
                "avatar-url",
                Role.USER,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    @Test
    @WithMockUser(roles = "USER")
    void getProfile_ShouldReturn200_WhenAuthenticated() throws Exception {
        when(profileServicePort.getMyProfile(any())).thenReturn(buildProfileResponse());

        mockMvc.perform(get("/api/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void getProfile_ShouldReturn401_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/profile"))
                                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateProfile_ShouldReturn200_WhenValid() throws Exception {
        when(profileServicePort.updateMyProfile(any(), any(UpdateProfileRequest.class)))
                .thenReturn(buildProfileResponse());

        UpdateProfileRequest request = new UpdateProfileRequest("New Name");

        mockMvc.perform(put("/api/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateProfile_ShouldReturn400_WhenDisplayNameBlank() throws Exception {
        UpdateProfileRequest request = new UpdateProfileRequest(" ");

        mockMvc.perform(put("/api/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void updateProfile_ShouldReturn401_WhenNotAuthenticated() throws Exception {
        UpdateProfileRequest request = new UpdateProfileRequest("New Name");

        mockMvc.perform(put("/api/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void changePassword_ShouldReturn200_WhenValid() throws Exception {
        doNothing().when(profileServicePort).changePassword(any(), any(ChangePasswordRequest.class));

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("old-pass");
        request.setNewPassword("new-password");

        mockMvc.perform(put("/api/profile/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "USER")
    void changePassword_ShouldReturn400_WhenCurrentPasswordInvalid() throws Exception {
        doThrow(new InvalidCurrentPasswordException("Invalid current password"))
                .when(profileServicePort).changePassword(any(), any(ChangePasswordRequest.class));

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("bad-pass");
        request.setNewPassword("new-password");

        mockMvc.perform(put("/api/profile/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "USER")
    void changePassword_ShouldReturn400_WhenNewPasswordTooShort() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("old-pass");
        request.setNewPassword("short");

        mockMvc.perform(put("/api/profile/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void changePassword_ShouldReturn401_WhenNotAuthenticated() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("old-pass");
        request.setNewPassword("new-password");

        mockMvc.perform(put("/api/profile/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateAvatar_ShouldReturn200_WhenValid() throws Exception {
        when(profileServicePort.updateAvatar(any(), any())).thenReturn(buildProfileResponse());

        MockMultipartFile avatar = new MockMultipartFile(
                "avatar",
                "avatar.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "img".getBytes());

        mockMvc.perform(multipart("/api/profile/avatar")
                        .file(avatar)
                        .with(req -> { req.setMethod("PATCH"); return req; }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateAvatar_ShouldReturn400_WhenMissingFile() throws Exception {
        mockMvc.perform(multipart("/api/profile/avatar")
                        .with(req -> { req.setMethod("PATCH"); return req; }))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateAvatar_ShouldReturn401_WhenNotAuthenticated() throws Exception {
        MockMultipartFile avatar = new MockMultipartFile(
                "avatar",
                "avatar.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "img".getBytes());

        mockMvc.perform(multipart("/api/profile/avatar")
                        .file(avatar)
                        .with(req -> { req.setMethod("PATCH"); return req; }))
                .andExpect(status().isForbidden());
    }
}
