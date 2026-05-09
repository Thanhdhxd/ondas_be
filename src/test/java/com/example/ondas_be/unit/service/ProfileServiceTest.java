package com.example.ondas_be.unit.service;

import com.example.ondas_be.application.dto.request.ChangePasswordRequest;
import com.example.ondas_be.application.dto.request.UpdateProfileRequest;
import com.example.ondas_be.application.dto.response.UserProfileResponse;
import com.example.ondas_be.application.exception.InvalidCurrentPasswordException;
import com.example.ondas_be.application.exception.UserNotFoundException;
import com.example.ondas_be.application.service.impl.ProfileService;
import com.example.ondas_be.application.service.port.StoragePort;
import com.example.ondas_be.domain.entity.Role;
import com.example.ondas_be.domain.entity.User;
import com.example.ondas_be.domain.repoport.RefreshTokenRepoPort;
import com.example.ondas_be.domain.repoport.UserRepoPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private UserRepoPort userRepoPort;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RefreshTokenRepoPort refreshTokenRepoPort;

    @Mock
    private StoragePort storagePort;

    @InjectMocks
    private ProfileService profileService;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(profileService, "imageBucket", "ondas-images");
    }

    private User buildUser(UUID id) {
        return new User(
                id,
                "user@example.com",
                "hash",
                "User",
                "old-avatar-url",
                true,
                null,
                null,
                null,
                Role.USER,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    @Test
    void getMyProfile_WhenValid_ShouldReturnProfile() {
        UUID userId = UUID.randomUUID();
        User user = buildUser(userId);

        when(userRepoPort.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        UserProfileResponse response = profileService.getMyProfile("user@example.com");

        assertEquals("user@example.com", response.getEmail());
        assertEquals(userId, response.getId());
    }

    @Test
    void getMyProfile_WhenUserNotFound_ShouldThrow() {
        when(userRepoPort.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> profileService.getMyProfile("missing@example.com"));
    }

    @Test
    void updateMyProfile_WhenValid_ShouldTrimAndSave() {
        UUID userId = UUID.randomUUID();
        User user = buildUser(userId);

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setDisplayName("  New Name  ");

        when(userRepoPort.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(userRepoPort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        profileService.updateMyProfile("user@example.com", request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepoPort).save(captor.capture());
        assertEquals("New Name", captor.getValue().getDisplayName());
    }

    @Test
    void updateMyProfile_WhenUserNotFound_ShouldThrow() {
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setDisplayName("New Name");

        when(userRepoPort.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> profileService.updateMyProfile("missing@example.com", request));
    }

    @Test
    void updateAvatar_WhenValid_ShouldUploadAndDeleteOld() {
        UUID userId = UUID.randomUUID();
        User user = buildUser(userId);

        MockMultipartFile avatar = new MockMultipartFile(
                "avatar",
                "avatar.jpg",
                "image/jpeg",
                "avatar".getBytes()
        );

        when(userRepoPort.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(storagePort.upload(eq("ondas-images"), any(), any(), anyLong(), any()))
                .thenReturn("new-avatar-url");
        when(storagePort.extractObjectName(eq("ondas-images"), eq("old-avatar-url"))).thenReturn("old.jpg");
        when(userRepoPort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserProfileResponse response = profileService.updateAvatar("user@example.com", avatar);

        assertEquals("new-avatar-url", response.getAvatarUrl());
        verify(storagePort).delete("ondas-images", "old.jpg");
    }

    @Test
    void updateAvatar_WhenUserNotFound_ShouldThrow() {
        MockMultipartFile avatar = new MockMultipartFile(
                "avatar",
                "avatar.jpg",
                "image/jpeg",
                "avatar".getBytes()
        );

        when(userRepoPort.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> profileService.updateAvatar("missing@example.com", avatar));
    }

    @Test
    void changePassword_WhenValid_ShouldUpdateAndRevokeTokens() {
        UUID userId = UUID.randomUUID();
        User user = buildUser(userId);

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("old-pass");
        request.setNewPassword("new-pass");

        when(userRepoPort.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old-pass", "hash")).thenReturn(true);
        when(passwordEncoder.encode("new-pass")).thenReturn("new-hash");

        profileService.changePassword("user@example.com", request);

        verify(userRepoPort).save(any(User.class));
        verify(refreshTokenRepoPort).revokeAllByUserId(userId);
    }

    @Test
    void changePassword_WhenCurrentPasswordInvalid_ShouldThrow() {
        UUID userId = UUID.randomUUID();
        User user = buildUser(userId);

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("wrong");
        request.setNewPassword("new-pass");

        when(userRepoPort.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hash")).thenReturn(false);

        assertThrows(InvalidCurrentPasswordException.class,
                () -> profileService.changePassword("user@example.com", request));

        verify(userRepoPort, never()).save(any(User.class));
    }
}
