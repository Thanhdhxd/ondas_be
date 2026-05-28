package com.example.ondas_be.application.service.impl;

import com.example.ondas_be.application.dto.request.ChangePasswordRequest;
import com.example.ondas_be.application.dto.request.UpdateProfileRequest;
import com.example.ondas_be.application.dto.response.UserProfileResponse;
import com.example.ondas_be.application.exception.ErrorCodes;
import com.example.ondas_be.application.exception.InvalidCurrentPasswordException;
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
                .orElseThrow(() -> new UserNotFoundException(ErrorCodes.ERROR_USER_NOT_FOUND));

        return toProfileResponse(user);
    }

    @Override
    @Transactional
    public UserProfileResponse updateMyProfile(String email, UpdateProfileRequest request) {
        User existing = userRepoPort.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(ErrorCodes.ERROR_USER_NOT_FOUND));

        User updated = new User(
                existing.getId(),
                existing.getEmail(),
                existing.getPasswordHash(),
                request.getDisplayName().trim(),
                existing.getAvatarUrl(),
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

    @Override
    @Transactional
    public UserProfileResponse updateAvatar(String email, MultipartFile avatarFile) {
        User existing = userRepoPort.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(ErrorCodes.ERROR_USER_NOT_FOUND));

        String newAvatarUrl = uploadImage(avatarFile, "users/avatar/");

        // Xóa avatar cũ nếu có
        if (existing.getAvatarUrl() != null) {
            deleteObject(existing.getAvatarUrl());
        }

        User updated = new User(
                existing.getId(),
                existing.getEmail(),
                existing.getPasswordHash(),
                existing.getDisplayName(),
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

    private String uploadImage(MultipartFile file, String prefix) {
        String objectName = prefix + UUID.randomUUID() + resolveExtension(file.getOriginalFilename());
        try {
            return storagePort.upload(imageBucket, objectName, file.getInputStream(), file.getSize(), file.getContentType());
        } catch (IOException ex) {
            throw new StorageOperationException("Cannot read upload stream", ex);
        }
    }

    private void deleteObject(String url) {
        String objectName = storagePort.extractObjectName(imageBucket, url);
        storagePort.delete(imageBucket, objectName);
    }

    private String resolveExtension(String originalFilename) {
        if (originalFilename != null && originalFilename.contains(".")) {
            return originalFilename.substring(originalFilename.lastIndexOf('.'));
        }
        return "";
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
                .orElseThrow(() -> new UserNotFoundException(ErrorCodes.ERROR_USER_NOT_FOUND));

        // Xác minh mật khẩu hiện tại
        if (!passwordEncoder.matches(request.getCurrentPassword(), existing.getPasswordHash())) {
            throw new InvalidCurrentPasswordException(ErrorCodes.ERROR_CURRENT_PASSWORD_INVALID);
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
}
