package com.example.ondas_be.application.service.impl;

import com.example.ondas_be.application.dto.common.PageResultDto;
import com.example.ondas_be.application.dto.request.AdminUserFilterRequest;
import com.example.ondas_be.application.dto.request.BanUserRequest;
import com.example.ondas_be.application.dto.response.AdminUserResponse;
import com.example.ondas_be.application.exception.UserNotFoundException;
import com.example.ondas_be.application.service.port.AdminUserServicePort;
import com.example.ondas_be.domain.entity.User;
import com.example.ondas_be.domain.repoport.AdminUserRepoPort;
import com.example.ondas_be.domain.repoport.UserRepoPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminUserService implements AdminUserServicePort {

    private final UserRepoPort userRepoPort;         // dùng cho getById / ban / unban
    private final AdminUserRepoPort adminUserRepoPort; // dùng cho list + filter

    @Override
    @Transactional(readOnly = true)
    public PageResultDto<AdminUserResponse> getUsers(AdminUserFilterRequest filter) {
        String keyword = (filter.getKeyword() == null || filter.getKeyword().isBlank())
                ? null : filter.getKeyword().trim();

        // role phải truyền dưới dạng String vì native query không hiểu Java enum
        String roleStr = (filter.getRole() != null) ? filter.getRole().name() : null;

        Page<User> page = adminUserRepoPort.findAllWithFilters(
                keyword,
                roleStr,
                filter.getActive(),
                PageRequest.of(filter.getPage(), filter.getSize())
        );

        return PageResultDto.<AdminUserResponse>builder()
                .items(page.map(this::toAdminResponse).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AdminUserResponse getUserById(UUID id) {
        User user = userRepoPort.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        return toAdminResponse(user);
    }

    @Override
    @Transactional
    public AdminUserResponse banUser(UUID id, BanUserRequest request) {
        User existing = userRepoPort.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        User banned = new User(
                existing.getId(),
                existing.getEmail(),
                existing.getPasswordHash(),
                existing.getDisplayName(),
                existing.getAvatarUrl(),
                false,                          // active = false
                request.getBanReason(),
                LocalDateTime.now(),            // bannedAt
                existing.getLastLoginAt(),
                existing.getRole(),
                existing.getCreatedAt(),
                existing.getUpdatedAt()
        );

        return toAdminResponse(userRepoPort.save(banned));
    }

    @Override
    @Transactional
    public AdminUserResponse unbanUser(UUID id) {
        User existing = userRepoPort.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        User unbanned = new User(
                existing.getId(),
                existing.getEmail(),
                existing.getPasswordHash(),
                existing.getDisplayName(),
                existing.getAvatarUrl(),
                true,     // active = true
                null,     // banReason cleared
                null,     // bannedAt cleared
                existing.getLastLoginAt(),
                existing.getRole(),
                existing.getCreatedAt(),
                existing.getUpdatedAt()
        );

        return toAdminResponse(userRepoPort.save(unbanned));
    }

    // ── helper ──────────────────────────────────────────────────────────────
    private AdminUserResponse toAdminResponse(User user) {
        return new AdminUserResponse(
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                user.getAvatarUrl(),
                user.getRole(),
                user.isActive(),
                user.getBanReason(),
                user.getBannedAt(),
                user.getLastLoginAt(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
