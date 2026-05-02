package com.example.ondas_be.presentation.controller;

import com.example.ondas_be.application.dto.common.ApiResponse;
import com.example.ondas_be.application.dto.common.PageResultDto;
import com.example.ondas_be.application.dto.request.AdminUserFilterRequest;
import com.example.ondas_be.application.dto.request.BanUserRequest;
import com.example.ondas_be.application.dto.response.AdminUserResponse;
import com.example.ondas_be.application.service.port.AdminUserServicePort;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserServicePort adminUserServicePort;

    /**
     * GET /api/admin/users
     * Danh sách user (có filter: keyword, role, active) + phân trang
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResultDto<AdminUserResponse>>> getUsers(
            @ModelAttribute AdminUserFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(adminUserServicePort.getUsers(filter)));
    }

    /**
     * GET /api/admin/users/{id}
     * Xem chi tiết một user
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminUserResponse>> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(adminUserServicePort.getUserById(id)));
    }

    /**
     * PATCH /api/admin/users/{id}/ban
     * Ban một user (yêu cầu lý do)
     */
    @PatchMapping("/{id}/ban")
    public ResponseEntity<ApiResponse<AdminUserResponse>> banUser(
            @PathVariable UUID id,
            @Valid @RequestBody BanUserRequest request) {
        return ResponseEntity.ok(ApiResponse.success(adminUserServicePort.banUser(id, request)));
    }

    /**
     * PATCH /api/admin/users/{id}/unban
     * Unban một user
     */
    @PatchMapping("/{id}/unban")
    public ResponseEntity<ApiResponse<AdminUserResponse>> unbanUser(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(adminUserServicePort.unbanUser(id)));
    }
}
