package com.example.ondas_be.application.service.impl;

import com.example.ondas_be.application.exception.StorageOperationException;
import com.example.ondas_be.application.exception.UserNotFoundException;
import com.example.ondas_be.application.service.port.ProfileServicePort;
import com.example.ondas_be.application.service.port.StoragePort;
import com.example.ondas_be.domain.entity.User;
import com.example.ondas_be.domain.repoport.RefreshTokenRepoPort;
import com.example.ondas_be.domain.repoport.UserRepoPort;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;
import com.example.ondas_be.application.dto.request.ChangePasswordRequest;
import com.example.ondas_be.application.dto.request.UpdateProfileRequest;
import com.example.ondas_be.application.dto.response.UserProfileResponse;
import com.example.ondas_be.application.exception.InvalidCurrentPasswordException;
import com.example.ondas_be.application.exception.StorageOperationException;
import com.example.ondas_be.application.service.port.ProfileServicePort;
import com.example.ondas_be.application.service.port.StoragePort;
import com.example.ondas_be.domain.entity.User;
import com.example.ondas_be.domain.repoport.RefreshTokenRepoPort;
import com.example.ondas_be.domain.repoport.UserRepoPort;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileService implements ProfileServicePort {

    private final UserRepoPort userRepoPort;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepoPort refreshTokenRepoPort;
    private final StoragePort storagePort;

    @Value("${storage.minio.bucket-image}")
    private String imageBucket;

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
                existing.getRole(),
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
                user.getRole(),
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
                existing.getRole(),
                existing.getCreatedAt(),
                existing.getUpdatedAt());

        userRepoPort.save(updated);

        // Thu hồi tất cả refresh token — buộc đăng nhập lại trên tất cả thiết bị
        refreshTokenRepoPort.revokeAllByUserId(existing.getId());
    }

    @Override
    @Transactional
    public UserProfileResponse uploadAvatar(String email, MultipartFile avatarFile) {
        User existing = userRepoPort.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        if (avatarFile == null || avatarFile.isEmpty()) {
            throw new IllegalArgumentException("Avatar file is empty");
        }

        String objectName = "users/avatar/" + UUID.randomUUID() + resolveExtension(avatarFile.getOriginalFilename());
        String avatarUrl;
        try {
            avatarUrl = storagePort.upload(imageBucket, objectName, avatarFile.getInputStream(), avatarFile.getSize(),
                    avatarFile.getContentType());
        } catch (IOException ex) {
            throw new StorageOperationException("Cannot read upload stream", ex);
        }

        // Xóa ảnh cũ nếu có
        if (existing.getAvatarUrl() != null && !existing.getAvatarUrl().isBlank()) {
            try {
                String oldObjectName = storagePort.extractObjectName(imageBucket, existing.getAvatarUrl());
                storagePort.delete(imageBucket, oldObjectName);
            } catch (Exception e) {
                // Ignore error if old file not found or deletion fails
            }
        }

        User updated = new User(
                existing.getId(),
                existing.getEmail(),
                existing.getPasswordHash(),
                existing.getDisplayName(),
                avatarUrl,
                existing.isActive(),
                existing.getBanReason(),
                existing.getBannedAt(),
                existing.getLastLoginAt(),
                existing.getRole(),
                existing.getCreatedAt(),
                LocalDateTime.now()); // updatedAt

        User saved = userRepoPort.save(updated);
        return toProfileResponse(saved);
    }

    private String resolveExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return "";
        }
        return originalFilename.substring(originalFilename.lastIndexOf('.')).toLowerCase();
    }
}
