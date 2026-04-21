package com.example.ondas_be.unit.service;

import com.example.ondas_be.application.dto.request.ResetPasswordRequest;
import com.example.ondas_be.application.exception.InvalidTokenException;
import com.example.ondas_be.application.mapper.AuthMapper;
import com.example.ondas_be.application.service.impl.AuthService;
import com.example.ondas_be.application.service.port.EmailPort;
import com.example.ondas_be.domain.entity.OtpCode;
import com.example.ondas_be.domain.entity.Role;
import com.example.ondas_be.domain.entity.User;
import com.example.ondas_be.domain.repoport.OtpCodeRepoPort;
import com.example.ondas_be.domain.repoport.RefreshTokenRepoPort;
import com.example.ondas_be.domain.repoport.UserRepoPort;
import com.example.ondas_be.infrastructure.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceResetPasswordTest {

    @Mock
    private UserRepoPort userRepoPort;

    @Mock
    private RefreshTokenRepoPort refreshTokenRepoPort;

    @Mock
    private OtpCodeRepoPort otpCodeRepoPort;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthMapper authMapper;

    @Mock
    private EmailPort emailPort;

    @InjectMocks
    private AuthService authService;

    @Test
    void resetPassword_WhenOtpValid_ShouldUpdatePasswordAndRevokeSessions() {
        UUID userId = UUID.randomUUID();
        UUID otpId = UUID.randomUUID();
        String rawOtp = "123456";
        String otpHash = hashToken(rawOtp);

        User existingUser = buildUser(userId, "old-hash");
        OtpCode otpCode = new OtpCode(
                otpId,
                userId,
                otpHash,
                LocalDateTime.now().plusMinutes(1),
                false,
                LocalDateTime.now()
        );

        when(userRepoPort.findByEmail("user@example.com")).thenReturn(Optional.of(existingUser));
        when(otpCodeRepoPort.findActiveByUserIdAndCodeHash(userId, otpHash)).thenReturn(Optional.of(otpCode));
        when(passwordEncoder.encode("new-password-123")).thenReturn("new-hash");
        when(userRepoPort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        authService.resetPassword(new ResetPasswordRequest("user@example.com", rawOtp, "new-password-123"));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepoPort).save(userCaptor.capture());
        assertEquals("new-hash", userCaptor.getValue().getPasswordHash());

        verify(otpCodeRepoPort).markAsUsed(otpId);
        verify(refreshTokenRepoPort).revokeAllByUserId(userId);
    }

    @Test
    void resetPassword_WhenOtpExpired_ShouldThrowInvalidTokenException() {
        UUID userId = UUID.randomUUID();
        UUID otpId = UUID.randomUUID();
        String rawOtp = "654321";
        String otpHash = hashToken(rawOtp);

        User existingUser = buildUser(userId, "old-hash");
        OtpCode expiredOtpCode = new OtpCode(
                otpId,
                userId,
                otpHash,
                LocalDateTime.now().minusSeconds(1),
                false,
                LocalDateTime.now().minusMinutes(1)
        );

        when(userRepoPort.findByEmail("user@example.com")).thenReturn(Optional.of(existingUser));
        when(otpCodeRepoPort.findActiveByUserIdAndCodeHash(userId, otpHash)).thenReturn(Optional.of(expiredOtpCode));

        assertThrows(InvalidTokenException.class,
                () -> authService.resetPassword(new ResetPasswordRequest("user@example.com", rawOtp, "new-password-123")));

        verify(otpCodeRepoPort).markAsUsed(otpId);
        verify(userRepoPort, never()).save(any(User.class));
        verify(refreshTokenRepoPort, never()).revokeAllByUserId(any(UUID.class));
    }

    @Test
    void resetPassword_WhenOtpNotFound_ShouldThrowInvalidTokenException() {
        UUID userId = UUID.randomUUID();
        String rawOtp = "000111";
        String otpHash = hashToken(rawOtp);

        User existingUser = buildUser(userId, "old-hash");

        when(userRepoPort.findByEmail("user@example.com")).thenReturn(Optional.of(existingUser));
        when(otpCodeRepoPort.findActiveByUserIdAndCodeHash(userId, otpHash)).thenReturn(Optional.empty());

        assertThrows(InvalidTokenException.class,
                () -> authService.resetPassword(new ResetPasswordRequest("user@example.com", rawOtp, "new-password-123")));

        verify(userRepoPort, never()).save(any(User.class));
        verify(otpCodeRepoPort, never()).markAsUsed(any(UUID.class));
        verify(refreshTokenRepoPort, never()).revokeAllByUserId(any(UUID.class));
    }

    private User buildUser(UUID userId, String passwordHash) {
        return new User(
                userId,
                "user@example.com",
                passwordHash,
                "Test User",
                null,
                true,
                null,
                null,
                null,
                Role.USER,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashedToken = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashedToken);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
