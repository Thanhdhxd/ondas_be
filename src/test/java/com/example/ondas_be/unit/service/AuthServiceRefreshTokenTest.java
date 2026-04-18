package com.example.ondas_be.unit.service;

import com.example.ondas_be.application.dto.request.RefreshTokenRequest;
import com.example.ondas_be.application.dto.response.AuthResponse;
import com.example.ondas_be.application.dto.response.UserSummaryResponse;
import com.example.ondas_be.application.exception.InvalidTokenException;
import com.example.ondas_be.application.mapper.AuthMapper;
import com.example.ondas_be.application.service.impl.AuthService;
import com.example.ondas_be.domain.entity.RefreshToken;
import com.example.ondas_be.domain.entity.Role;
import com.example.ondas_be.domain.entity.User;
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
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HexFormat;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceRefreshTokenTest {

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
    void refreshToken_WhenValid_ShouldRotateAndReturnAuthResponse() {
        UUID userId = UUID.randomUUID();
        String rawRefreshToken = "refresh-token-value";
        String tokenHash = hashToken(rawRefreshToken);

        RefreshToken storedRefreshToken = new RefreshToken(
                UUID.randomUUID(),
                userId,
                tokenHash,
                LocalDateTime.now().plusDays(1),
                false,
                LocalDateTime.now()
        );

        User user = new User(
                userId,
                "user@example.com",
                "hashed-password",
                "Test User",
                null,
                true,
                null,
                null,
                LocalDateTime.now(),
                Set.of(Role.USER),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        AuthResponse expected = new AuthResponse(
                "new-access-token",
                "new-refresh-token",
                new UserSummaryResponse(userId, "user@example.com", "Test User", Set.of(Role.USER))
        );

        when(jwtUtil.isRefreshTokenValid(rawRefreshToken)).thenReturn(true);
        when(refreshTokenRepoPort.findByTokenHash(tokenHash)).thenReturn(Optional.of(storedRefreshToken));
        when(userRepoPort.findById(userId)).thenReturn(Optional.of(user));
        when(jwtUtil.generateAccessToken(eq("user@example.com"), eq(Set.of(Role.USER)))).thenReturn("new-access-token");
        when(jwtUtil.generateRefreshToken(eq("user@example.com"), eq(Set.of(Role.USER)))).thenReturn("new-refresh-token");
        when(jwtUtil.extractExpiration("new-refresh-token")).thenReturn(Date.from(Instant.now().plusSeconds(3600)));
        when(refreshTokenRepoPort.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(authMapper.toAuthResponse(user, "new-access-token", "new-refresh-token")).thenReturn(expected);

        AuthResponse result = authService.refreshToken(new RefreshTokenRequest(rawRefreshToken));

        assertEquals("new-access-token", result.getAccessToken());
        assertEquals("new-refresh-token", result.getRefreshToken());
        verify(refreshTokenRepoPort).revokeAllByUserId(userId);
        verify(refreshTokenRepoPort).save(any(RefreshToken.class));
    }

    @Test
    void refreshToken_WhenJwtInvalid_ShouldThrowInvalidTokenException() {
        when(jwtUtil.isRefreshTokenValid("invalid")).thenReturn(false);

        assertThrows(InvalidTokenException.class,
                () -> authService.refreshToken(new RefreshTokenRequest("invalid")));

        verify(refreshTokenRepoPort, never()).findByTokenHash(any());
        verify(userRepoPort, never()).findById(any());
    }

    @Test
    void refreshToken_WhenStoredTokenRevoked_ShouldThrowInvalidTokenException() {
        UUID userId = UUID.randomUUID();
        String rawRefreshToken = "revoked-refresh-token";
        String tokenHash = hashToken(rawRefreshToken);

        RefreshToken storedRefreshToken = new RefreshToken(
                UUID.randomUUID(),
                userId,
                tokenHash,
                LocalDateTime.now().plusDays(1),
                true,
                LocalDateTime.now()
        );

        when(jwtUtil.isRefreshTokenValid(rawRefreshToken)).thenReturn(true);
        when(refreshTokenRepoPort.findByTokenHash(tokenHash)).thenReturn(Optional.of(storedRefreshToken));

        assertThrows(InvalidTokenException.class,
                () -> authService.refreshToken(new RefreshTokenRequest(rawRefreshToken)));

        verify(userRepoPort, never()).findById(any());
        verify(jwtUtil, never()).generateAccessToken(any(), any());
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
