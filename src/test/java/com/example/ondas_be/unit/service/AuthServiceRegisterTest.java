package com.example.ondas_be.unit.service;

import com.example.ondas_be.application.dto.request.RegisterRequest;
import com.example.ondas_be.application.dto.response.AuthResponse;
import com.example.ondas_be.application.dto.response.UserSummaryResponse;
import com.example.ondas_be.application.exception.EmailAlreadyExistsException;
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
import org.mockito.ArgumentCaptor;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceRegisterTest {

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
    void register_WhenValid_ShouldReturnAuthResponse() {
        RegisterRequest request = new RegisterRequest("  TEST@Example.COM ", "12345678", "  New User  ");

        UUID userId = UUID.randomUUID();
        User savedUser = new User(
                userId,
                "test@example.com",
                "hashed-password",
                "New User",
                null,
                true,
                null,
                null,
                null,
                Set.of(Role.USER),
                LocalDateTime.now(),
                LocalDateTime.now());

        AuthResponse expected = new AuthResponse(
                "access-token",
                "refresh-token",
                new UserSummaryResponse(userId, "test@example.com", "New User", Set.of(Role.USER)));

        when(userRepoPort.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("12345678")).thenReturn("hashed-password");
        when(userRepoPort.save(any(User.class))).thenReturn(savedUser);
        when(jwtUtil.generateAccessToken(eq("test@example.com"), eq(Set.of(Role.USER)))).thenReturn("access-token");
        when(jwtUtil.generateRefreshToken(eq("test@example.com"), eq(Set.of(Role.USER)))).thenReturn("refresh-token");
        when(jwtUtil.extractExpiration("refresh-token")).thenReturn(Date.from(Instant.now().plusSeconds(3600)));
        when(refreshTokenRepoPort.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(authMapper.toAuthResponse(savedUser, "access-token", "refresh-token")).thenReturn(expected);

        AuthResponse result = authService.register(request);

        assertEquals("access-token", result.getAccessToken());
        assertEquals("refresh-token", result.getRefreshToken());
        assertEquals("test@example.com", result.getUser().getEmail());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepoPort).save(userCaptor.capture());

        User userToSave = userCaptor.getValue();
        assertEquals("test@example.com", userToSave.getEmail());
        assertEquals("hashed-password", userToSave.getPasswordHash());
        assertEquals("New User", userToSave.getDisplayName());
        assertTrue(userToSave.isActive());
        assertEquals(Set.of(Role.USER), userToSave.getRoles());
        verify(refreshTokenRepoPort).revokeAllByUserId(userId);
        verify(refreshTokenRepoPort).save(any(RefreshToken.class));
    }

    @Test
    void register_WhenEmailExists_ShouldThrowEmailAlreadyExistsException() {
        RegisterRequest request = new RegisterRequest("dup@example.com", "12345678", "Duplicated");
        when(userRepoPort.existsByEmail("dup@example.com")).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> authService.register(request));

        verify(userRepoPort, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(any());
        verify(jwtUtil, never()).generateAccessToken(any(), any());
        verify(jwtUtil, never()).generateRefreshToken(any(), any());
        verify(authMapper, never()).toAuthResponse(any(User.class), any(), any());
        verify(refreshTokenRepoPort, never()).save(any(RefreshToken.class));
    }
}
