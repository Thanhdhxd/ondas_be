package com.example.ondas_be.unit.service;

import com.example.ondas_be.application.dto.request.LogoutRequest;
import com.example.ondas_be.application.mapper.AuthMapper;
import com.example.ondas_be.application.service.impl.AuthService;
import com.example.ondas_be.domain.repoport.RefreshTokenRepoPort;
import com.example.ondas_be.domain.repoport.UserRepoPort;
import com.example.ondas_be.infrastructure.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceLogoutTest {

    @Mock
    private UserRepoPort userRepoPort;

    @Mock
    private RefreshTokenRepoPort refreshTokenRepoPort;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthMapper authMapper;

    @InjectMocks
    private AuthService authService;

    @Test
    void logout_WhenJwtInvalid_ShouldReturnWithoutRevocation() {
        String rawRefreshToken = "invalid-refresh-token";
        when(jwtUtil.isRefreshTokenValid(rawRefreshToken)).thenReturn(false);

        assertDoesNotThrow(() -> authService.logout(new LogoutRequest(rawRefreshToken)));

        verify(refreshTokenRepoPort, never()).revokeByTokenHash(anyString());
    }

    @Test
    void logout_WhenJwtValid_ShouldRevokeByHashedToken() {
        String rawRefreshToken = "valid-refresh-token";
        String expectedHash = hashToken(rawRefreshToken);
        when(jwtUtil.isRefreshTokenValid(rawRefreshToken)).thenReturn(true);

        assertDoesNotThrow(() -> authService.logout(new LogoutRequest(rawRefreshToken)));

        verify(refreshTokenRepoPort).revokeByTokenHash(expectedHash);
    }

    @Test
    void logout_WhenJwtValidAndTokenNotFound_ShouldStillBeIdempotent() {
        String rawRefreshToken = "valid-but-not-found-refresh-token";
        when(jwtUtil.isRefreshTokenValid(rawRefreshToken)).thenReturn(true);

        assertDoesNotThrow(() -> authService.logout(new LogoutRequest(rawRefreshToken)));

        verify(refreshTokenRepoPort).revokeByTokenHash(hashToken(rawRefreshToken));
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
