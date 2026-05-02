package com.example.ondas_be.application.service.port;

import com.example.ondas_be.application.dto.common.PageResultDto;
import com.example.ondas_be.application.dto.request.AdminUserFilterRequest;
import com.example.ondas_be.application.dto.request.BanUserRequest;
import com.example.ondas_be.application.dto.response.AdminUserResponse;

import java.util.UUID;

public interface AdminUserServicePort {

    PageResultDto<AdminUserResponse> getUsers(AdminUserFilterRequest filter);

    AdminUserResponse getUserById(UUID id);

    AdminUserResponse banUser(UUID id, BanUserRequest request);

    AdminUserResponse unbanUser(UUID id);
}
