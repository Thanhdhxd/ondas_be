package com.example.ondas_be.unit.service;

import com.example.ondas_be.application.dto.request.LoginRequest;
import com.example.ondas_be.application.dto.response.AuthResponse;
import com.example.ondas_be.application.dto.response.UserSummaryResponse;
import com.example.ondas_be.application.exception.InvalidCredentialsException;
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

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
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
class AuthServiceLoginTest {

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
    void login_WhenCredentialsValid_ShouldReturnAuthResponse() {
        UUID userId = UUID.randomUUID();
        User existingUser = buildUser(userId, "user@example.com", "hashed-password", true, null);
        User updatedUser = buildUser(userId, "user@example.com", "hashed-password", true, LocalDateTime.now());

        AuthResponse expected = new AuthResponse(
                "access-token",
                "refresh-token",
                new UserSummaryResponse(userId, "user@example.com", "Test User", Set.of(Role.USER))
        );

        when(userRepoPort.findByEmail("user@example.com")).thenReturn(java.util.Optional.of(existingUser));
        when(passwordEncoder.matches("12345678", "hashed-password")).thenReturn(true);
        when(userRepoPort.save(any(User.class))).thenReturn(updatedUser);
        when(jwtUtil.generateAccessToken(eq("user@example.com"), eq(Set.of(Role.USER)))).thenReturn("access-token");
        when(jwtUtil.generateRefreshToken(eq("user@example.com"), eq(Set.of(Role.USER)))).thenReturn("refresh-token");
        when(jwtUtil.extractExpiration("refresh-token")).thenReturn(Date.from(Instant.now().plusSeconds(3600)));
        when(refreshTokenRepoPort.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(authMapper.toAuthResponse(updatedUser, "access-token", "refresh-token")).thenReturn(expected);

        AuthResponse result = authService.login(new LoginRequest("user@example.com", "12345678"));

        assertEquals("access-token", result.getAccessToken());
        assertEquals("refresh-token", result.getRefreshToken());
        assertEquals("user@example.com", result.getUser().getEmail());
        verify(refreshTokenRepoPort).revokeAllByUserId(userId);
        verify(refreshTokenRepoPort).save(any(RefreshToken.class));
    }

    @Test
    void login_WhenUserNotFound_ShouldThrowInvalidCredentialsException() {
        when(userRepoPort.findByEmail("missing@example.com")).thenReturn(java.util.Optional.empty());

        assertThrows(InvalidCredentialsException.class,
                () -> authService.login(new LoginRequest("missing@example.com", "12345678")));

        verify(passwordEncoder, never()).matches(any(), any());
        verify(userRepoPort, never()).save(any(User.class));
        verify(jwtUtil, never()).generateAccessToken(any(), any());
    }

    @Test
    void login_WhenPasswordInvalid_ShouldThrowInvalidCredentialsException() {
        UUID userId = UUID.randomUUID();
        User existingUser = buildUser(userId, "user@example.com", "hashed-password", true, null);

        when(userRepoPort.findByEmail("user@example.com")).thenReturn(java.util.Optional.of(existingUser));
        when(passwordEncoder.matches("wrong-password", "hashed-password")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class,
                () -> authService.login(new LoginRequest("user@example.com", "wrong-password")));

        verify(userRepoPort, never()).save(any(User.class));
        verify(jwtUtil, never()).generateAccessToken(any(), any());
        verify(refreshTokenRepoPort, never()).save(any(RefreshToken.class));
    }

    @Test
    void login_WhenUserInactive_ShouldThrowInvalidCredentialsException() {
        UUID userId = UUID.randomUUID();
        User existingUser = buildUser(userId, "user@example.com", "hashed-password", false, null);

        when(userRepoPort.findByEmail("user@example.com")).thenReturn(java.util.Optional.of(existingUser));

        assertThrows(InvalidCredentialsException.class,
                () -> authService.login(new LoginRequest("user@example.com", "12345678")));

        verify(passwordEncoder, never()).matches(any(), any());
        verify(jwtUtil, never()).generateAccessToken(any(), any());
    }

    private User buildUser(UUID userId, String email, String passwordHash, boolean active, LocalDateTime lastLoginAt) {
        return new User(
                userId,
                email,
                passwordHash,
                "Test User",
                null,
                active,
                null,
                null,
                lastLoginAt,
                Set.of(Role.USER),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
