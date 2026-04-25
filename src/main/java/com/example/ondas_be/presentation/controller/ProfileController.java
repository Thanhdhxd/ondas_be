package com.example.ondas_be.presentation.controller;

import com.example.ondas_be.application.dto.common.ApiResponse;
import com.example.ondas_be.application.dto.request.ChangePasswordRequest;
import com.example.ondas_be.application.dto.request.UpdateProfileRequest;
import com.example.ondas_be.application.dto.response.UserProfileResponse;
import com.example.ondas_be.application.service.port.ProfileServicePort;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileServicePort profileServicePort;

    @GetMapping
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        UserProfileResponse response = profileServicePort.getMyProfile(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateMyProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest request) {
        UserProfileResponse response = profileServicePort.updateMyProfile(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/avatar")
    public ResponseEntity<ApiResponse<UserProfileResponse>> uploadAvatar(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("avatar") MultipartFile avatarFile) {
        UserProfileResponse response = profileServicePort.uploadAvatar(userDetails.getUsername(), avatarFile);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request) {
        profileServicePort.changePassword(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
