package com.example.ondas_be.unit.service;

import com.example.ondas_be.application.dto.request.ForgotPasswordRequest;
import com.example.ondas_be.application.exception.UserNotFoundException;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceForgotPasswordTest {

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
    void forgotPassword_WhenEmailExists_ShouldGenerateOtpPersistAndSendEmail() {
        ReflectionTestUtils.setField(authService, "passwordResetExpirationMs", 60000L);

        UUID userId = UUID.randomUUID();
        User user = new User(
                userId,
                "user@example.com",
                "hashed-password",
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

        when(userRepoPort.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(otpCodeRepoPort.save(any(OtpCode.class))).thenAnswer(invocation -> invocation.getArgument(0));

        authService.forgotPassword(new ForgotPasswordRequest("user@example.com"));

        ArgumentCaptor<OtpCode> otpCodeCaptor = ArgumentCaptor.forClass(OtpCode.class);
        verify(otpCodeRepoPort).markAllUnusedByUserId(userId);
        verify(otpCodeRepoPort).save(otpCodeCaptor.capture());

        OtpCode savedOtpCode = otpCodeCaptor.getValue();
        assertNotNull(savedOtpCode.getCodeHash());
        assertEquals(userId, savedOtpCode.getUserId());

        ArgumentCaptor<String> otpCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailPort).sendPasswordResetOtp(eq("user@example.com"), eq("Test User"), otpCaptor.capture(), eq(1L));

        String rawOtp = otpCaptor.getValue();
        assertEquals(6, rawOtp.length());
        assertEquals(hashToken(rawOtp), savedOtpCode.getCodeHash());
    }

    @Test
    void forgotPassword_WhenUserNotFound_ShouldThrowUserNotFoundException() {
        ReflectionTestUtils.setField(authService, "passwordResetExpirationMs", 60000L);
        when(userRepoPort.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> authService.forgotPassword(new ForgotPasswordRequest("missing@example.com")));

        verify(otpCodeRepoPort, never()).save(any(OtpCode.class));
        verify(emailPort, never()).sendPasswordResetOtp(any(), any(), any(), anyLong());
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
