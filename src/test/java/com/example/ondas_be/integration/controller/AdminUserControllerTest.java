package com.example.ondas_be.integration.controller;

import com.example.ondas_be.application.dto.common.PageResultDto;
import com.example.ondas_be.application.dto.request.AdminUserFilterRequest;
import com.example.ondas_be.application.dto.request.BanUserRequest;
import com.example.ondas_be.application.dto.response.AdminUserResponse;
import com.example.ondas_be.application.exception.UserNotFoundException;
import com.example.ondas_be.application.service.port.AdminUserServicePort;
import com.example.ondas_be.domain.entity.Role;
import com.example.ondas_be.presentation.advice.GlobalExceptionHandler;
import com.example.ondas_be.presentation.controller.AdminUserController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminUserController.class)
@AutoConfigureMockMvc(addFilters = false)   // tắt JWT filter khi test controller
@Import(GlobalExceptionHandler.class)
class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AdminUserServicePort adminUserServicePort;

    // ── helper ──────────────────────────────────────────────────────────────

    private AdminUserResponse buildUserResponse(UUID id, boolean active, String banReason) {
        return new AdminUserResponse(
                id,
                "user@example.com",
                "Test User",
                null,
                Role.USER,
                active,
                banReason,
                active ? null : LocalDateTime.now(),
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().minusDays(30),
                LocalDateTime.now()
        );
    }

    // ── GET /api/admin/users ─────────────────────────────────────────────────

    @Test
    void getUsers_ShouldReturn200WithPageResult() throws Exception {
        UUID id = UUID.randomUUID();
        AdminUserResponse userResponse = buildUserResponse(id, true, null);

        PageResultDto<AdminUserResponse> page = PageResultDto.<AdminUserResponse>builder()
                .items(List.of(userResponse))
                .page(0).size(20)
                .totalElements(1).totalPages(1)
                .build();

        when(adminUserServicePort.getUsers(any(AdminUserFilterRequest.class))).thenReturn(page);

        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.items[0].email").value("user@example.com"))
                .andExpect(jsonPath("$.data.items[0].active").value(true));
    }

    @Test
    void getUsers_ShouldReturn200WithEmptyList_WhenNoMatch() throws Exception {
        PageResultDto<AdminUserResponse> emptyPage = PageResultDto.<AdminUserResponse>builder()
                .items(List.of())
                .page(0).size(20)
                .totalElements(0).totalPages(0)
                .build();

        when(adminUserServicePort.getUsers(any(AdminUserFilterRequest.class))).thenReturn(emptyPage);

        mockMvc.perform(get("/api/admin/users").param("keyword", "nonexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(0))
                .andExpect(jsonPath("$.data.items").isEmpty());
    }

    // ── GET /api/admin/users/{id} ────────────────────────────────────────────

    @Test
    void getUserById_ShouldReturn200_WhenUserExists() throws Exception {
        UUID id = UUID.randomUUID();
        AdminUserResponse response = buildUserResponse(id, true, null);

        when(adminUserServicePort.getUserById(id)).thenReturn(response);

        mockMvc.perform(get("/api/admin/users/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("user@example.com"))
                .andExpect(jsonPath("$.data.active").value(true))
                .andExpect(jsonPath("$.data.banReason").doesNotExist());
    }

    @Test
    void getUserById_ShouldReturn404_WhenUserNotFound() throws Exception {
        UUID unknownId = UUID.randomUUID();
        when(adminUserServicePort.getUserById(unknownId))
                .thenThrow(new UserNotFoundException("User not found with id: " + unknownId));

        mockMvc.perform(get("/api/admin/users/{id}", unknownId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ── PATCH /api/admin/users/{id}/ban ──────────────────────────────────────

    @Test
    void banUser_ShouldReturn200_WhenUserExistsAndReasonProvided() throws Exception {
        UUID id = UUID.randomUUID();
        BanUserRequest request = new BanUserRequest();
        request.setBanReason("Violated community guidelines");

        AdminUserResponse response = buildUserResponse(id, false, "Violated community guidelines");

        when(adminUserServicePort.banUser(eq(id), any(BanUserRequest.class))).thenReturn(response);

        mockMvc.perform(patch("/api/admin/users/{id}/ban", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.active").value(false))
                .andExpect(jsonPath("$.data.banReason").value("Violated community guidelines"));
    }

    @Test
    void banUser_ShouldReturn400_WhenBanReasonIsBlank() throws Exception {
        UUID id = UUID.randomUUID();
        BanUserRequest request = new BanUserRequest();
        request.setBanReason("   ");   // blank → validation fail

        mockMvc.perform(patch("/api/admin/users/{id}/ban", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void banUser_ShouldReturn404_WhenUserNotFound() throws Exception {
        UUID unknownId = UUID.randomUUID();
        BanUserRequest request = new BanUserRequest();
        request.setBanReason("Reason");

        when(adminUserServicePort.banUser(eq(unknownId), any(BanUserRequest.class)))
                .thenThrow(new UserNotFoundException("User not found with id: " + unknownId));

        mockMvc.perform(patch("/api/admin/users/{id}/ban", unknownId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ── PATCH /api/admin/users/{id}/unban ────────────────────────────────────

    @Test
    void unbanUser_ShouldReturn200_AndClearBanInfo() throws Exception {
        UUID id = UUID.randomUUID();
        AdminUserResponse response = buildUserResponse(id, true, null);

        when(adminUserServicePort.unbanUser(id)).thenReturn(response);

        mockMvc.perform(patch("/api/admin/users/{id}/unban", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.active").value(true))
                .andExpect(jsonPath("$.data.banReason").doesNotExist());
    }

    @Test
    void unbanUser_ShouldReturn404_WhenUserNotFound() throws Exception {
        UUID unknownId = UUID.randomUUID();
        when(adminUserServicePort.unbanUser(unknownId))
                .thenThrow(new UserNotFoundException("User not found with id: " + unknownId));

        mockMvc.perform(patch("/api/admin/users/{id}/unban", unknownId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }
}
