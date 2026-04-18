package com.example.ondas_be.application.service.impl;

import com.example.ondas_be.application.dto.request.ChangePasswordRequest;
import com.example.ondas_be.application.dto.request.UpdateProfileRequest;
import com.example.ondas_be.application.dto.response.UserProfileResponse;
import com.example.ondas_be.application.exception.InvalidCurrentPasswordException;
import com.example.ondas_be.application.exception.UserNotFoundException;
import com.example.ondas_be.application.service.port.ProfileServicePort;
import com.example.ondas_be.domain.entity.User;
import com.example.ondas_be.domain.repoport.RefreshTokenRepoPort;
import com.example.ondas_be.domain.repoport.UserRepoPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileService implements ProfileServicePort {

    private final UserRepoPort userRepoPort;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepoPort refreshTokenRepoPort;

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getMyProfile(String email) {
        User user = userRepoPort.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        return toProfileResponse(user);
    }

    @Override
    @Transactional
    public UserProfileResponse updateMyProfile(String email, UpdateProfileRequest request) {
        User existing = userRepoPort.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        // Chỉ cập nhật displayName và avatarUrl; avatarUrl giữ nguyên nếu request gửi
        // null
        String newAvatarUrl = request.getAvatarUrl() != null
                ? request.getAvatarUrl().trim()
                : existing.getAvatarUrl();

        User updated = new User(
                existing.getId(),
                existing.getEmail(),
                existing.getPasswordHash(),
                request.getDisplayName().trim(),
                newAvatarUrl,
                existing.isActive(),
                existing.getBanReason(),
                existing.getBannedAt(),
                existing.getLastLoginAt(),
                existing.getRoles(),
                existing.getCreatedAt(),
                existing.getUpdatedAt());

        User saved = userRepoPort.save(updated);
        return toProfileResponse(saved);
    }

    private UserProfileResponse toProfileResponse(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                user.getAvatarUrl(),
                user.getRoles(),
                user.getLastLoginAt(),
                user.getCreatedAt());
    }

    @Override
    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        User existing = userRepoPort.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        // Xác minh mật khẩu hiện tại
        if (!passwordEncoder.matches(request.getCurrentPassword(), existing.getPasswordHash())) {
            throw new InvalidCurrentPasswordException("Current password is incorrect");
        }

        String newPasswordHash = passwordEncoder.encode(request.getNewPassword());

        User updated = new User(
                existing.getId(),
                existing.getEmail(),
                newPasswordHash,
                existing.getDisplayName(),
                existing.getAvatarUrl(),
                existing.isActive(),
                existing.getBanReason(),
                existing.getBannedAt(),
                existing.getLastLoginAt(),
                existing.getRoles(),
                existing.getCreatedAt(),
                existing.getUpdatedAt());

        userRepoPort.save(updated);

        // Thu hồi tất cả refresh token — buộc đăng nhập lại trên tất cả thiết bị
        refreshTokenRepoPort.revokeAllByUserId(existing.getId());
    }
}
