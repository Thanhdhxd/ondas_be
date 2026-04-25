package com.example.ondas_be.application.service.port;

import com.example.ondas_be.application.dto.request.ChangePasswordRequest;
import com.example.ondas_be.application.dto.request.UpdateProfileRequest;
import com.example.ondas_be.application.dto.response.UserProfileResponse;

public interface ProfileServicePort {

    UserProfileResponse getMyProfile(String email);

    UserProfileResponse updateMyProfile(String email, UpdateProfileRequest request);

    UserProfileResponse uploadAvatar(String email, org.springframework.web.multipart.MultipartFile avatarFile);

    void changePassword(String email, ChangePasswordRequest request);
}
