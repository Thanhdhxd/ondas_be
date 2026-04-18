package com.example.ondas_be.application.service.impl;

import com.example.ondas_be.application.dto.request.ForgotPasswordRequest;
import com.example.ondas_be.application.dto.request.LoginRequest;
import com.example.ondas_be.application.dto.request.LogoutRequest;
import com.example.ondas_be.application.dto.request.RefreshTokenRequest;
import com.example.ondas_be.application.dto.request.ResetPasswordRequest;
import com.example.ondas_be.application.dto.request.RegisterRequest;
import com.example.ondas_be.application.dto.response.AuthResponse;
import com.example.ondas_be.application.exception.EmailAlreadyExistsException;
import com.example.ondas_be.application.exception.InvalidCredentialsException;
import com.example.ondas_be.application.exception.InvalidTokenException;
import com.example.ondas_be.application.exception.UserNotFoundException;
import com.example.ondas_be.application.mapper.AuthMapper;
import com.example.ondas_be.application.service.port.EmailPort;
import com.example.ondas_be.domain.entity.OtpCode;
import com.example.ondas_be.domain.entity.RefreshToken;
import com.example.ondas_be.application.service.port.AuthServicePort;
import com.example.ondas_be.domain.entity.Role;
import com.example.ondas_be.domain.entity.User;
import com.example.ondas_be.domain.repoport.OtpCodeRepoPort;
import com.example.ondas_be.domain.repoport.RefreshTokenRepoPort;
import com.example.ondas_be.domain.repoport.UserRepoPort;
import com.example.ondas_be.infrastructure.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService implements AuthServicePort {

    private static final String INVALID_CREDENTIALS_MESSAGE = "Invalid credentials";
    private static final String INVALID_REFRESH_TOKEN_MESSAGE = "Invalid refresh token";
    private static final String INVALID_OTP_MESSAGE = "Invalid or expired OTP";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepoPort userRepoPort;
    private final RefreshTokenRepoPort refreshTokenRepoPort;
    private final OtpCodeRepoPort otpCodeRepoPort;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthMapper authMapper;
    private final EmailPort emailPort;

    @Value("${jwt.password-reset-expiration:60000}")
    private long passwordResetExpirationMs;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = normalizeEmail(request.getEmail());
        if (userRepoPort.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyExistsException("Email already exists");
        }

        User userToCreate = new User(
                null,
                normalizedEmail,
                passwordEncoder.encode(request.getPassword()),
                normalizeDisplayName(request.getDisplayName()),
                null,
                true,
                null,
                null,
                null,
                Set.of(Role.USER),
                null,
                null
        );

        User savedUser = userRepoPort.save(userToCreate);
        return issueTokenPair(savedUser);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = normalizeEmail(request.getEmail());
        User existingUser = userRepoPort.findByEmail(normalizedEmail)
                .orElseThrow(() -> new InvalidCredentialsException(INVALID_CREDENTIALS_MESSAGE));

        if (!existingUser.isActive()) {
            throw new InvalidCredentialsException(INVALID_CREDENTIALS_MESSAGE);
        }

        String passwordHash = existingUser.getPasswordHash();
        if (passwordHash == null || !passwordEncoder.matches(request.getPassword(), passwordHash)) {
            throw new InvalidCredentialsException(INVALID_CREDENTIALS_MESSAGE);
        }

        User userWithLastLoginUpdated = updateLastLogin(existingUser);
        return issueTokenPair(userWithLastLoginUpdated);
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String rawRefreshToken = request.getRefreshToken().trim();
        if (!jwtUtil.isRefreshTokenValid(rawRefreshToken)) {
            throw new InvalidTokenException(INVALID_REFRESH_TOKEN_MESSAGE);
        }

        String refreshTokenHash = hashToken(rawRefreshToken);
        RefreshToken existingRefreshToken = refreshTokenRepoPort.findByTokenHash(refreshTokenHash)
                .orElseThrow(() -> new InvalidTokenException(INVALID_REFRESH_TOKEN_MESSAGE));

        if (existingRefreshToken.isRevoked() || existingRefreshToken.isExpired()) {
            throw new InvalidTokenException(INVALID_REFRESH_TOKEN_MESSAGE);
        }

        User user = userRepoPort.findById(existingRefreshToken.getUserId())
                .orElseThrow(() -> new InvalidTokenException(INVALID_REFRESH_TOKEN_MESSAGE));

        if (!user.isActive()) {
            throw new InvalidTokenException(INVALID_REFRESH_TOKEN_MESSAGE);
        }

        return issueTokenPair(user);
    }

    @Override
    @Transactional
    public void logout(LogoutRequest request) {
        String rawRefreshToken = request.getRefreshToken().trim();
        if (!jwtUtil.isRefreshTokenValid(rawRefreshToken)) {
            return;
        }

        String refreshTokenHash = hashToken(rawRefreshToken);
        refreshTokenRepoPort.revokeByTokenHash(refreshTokenHash);
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        String normalizedEmail = normalizeEmail(request.getEmail());
        User user = userRepoPort.findByEmail(normalizedEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + normalizedEmail));

        String otp = generateOtp();
        String otpHash = hashToken(otp);

        otpCodeRepoPort.markAllUnusedByUserId(user.getId());
        OtpCode otpCode = new OtpCode(
                null,
                user.getId(),
                otpHash,
                LocalDateTime.now().plus(Duration.ofMillis(passwordResetExpirationMs)),
                false,
                null
        );
        otpCodeRepoPort.save(otpCode);

        emailPort.sendPasswordResetOtp(user.getEmail(), user.getDisplayName(), otp, toExpirationMinutes(passwordResetExpirationMs));
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String normalizedEmail = normalizeEmail(request.getEmail());
        User user = userRepoPort.findByEmail(normalizedEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + normalizedEmail));

        String otpHash = hashToken(request.getOtp().trim());
        OtpCode otpCode = otpCodeRepoPort.findActiveByUserIdAndCodeHash(user.getId(), otpHash)
                .orElseThrow(() -> new InvalidTokenException(INVALID_OTP_MESSAGE));

        if (otpCode.isExpired()) {
            otpCodeRepoPort.markAsUsed(otpCode.getId());
            throw new InvalidTokenException(INVALID_OTP_MESSAGE);
        }

        User userWithUpdatedPassword = updatePassword(user, request.getNewPassword());
        userRepoPort.save(userWithUpdatedPassword);

        otpCodeRepoPort.markAsUsed(otpCode.getId());
        refreshTokenRepoPort.revokeAllByUserId(user.getId());
    }

    private AuthResponse issueTokenPair(User user) {
        String accessToken = jwtUtil.generateAccessToken(user.getEmail(), user.getRoles());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail(), user.getRoles());
        persistRefreshToken(user.getId(), refreshToken);
        return authMapper.toAuthResponse(user, accessToken, refreshToken);
    }

    private void persistRefreshToken(UUID userId, String rawRefreshToken) {
        String tokenHash = hashToken(rawRefreshToken);
        refreshTokenRepoPort.revokeAllByUserId(userId);

        RefreshToken refreshToken = new RefreshToken(
                null,
                userId,
                tokenHash,
                LocalDateTime.ofInstant(jwtUtil.extractExpiration(rawRefreshToken).toInstant(), ZoneId.systemDefault()),
                false,
                null
        );

        refreshTokenRepoPort.save(refreshToken);
    }

    private User updateLastLogin(User user) {
        User userToUpdate = new User(
                user.getId(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getDisplayName(),
                user.getAvatarUrl(),
                user.isActive(),
                user.getBanReason(),
                user.getBannedAt(),
                LocalDateTime.now(),
                user.getRoles(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
        return userRepoPort.save(userToUpdate);
    }

    private User updatePassword(User user, String rawPassword) {
        return new User(
                user.getId(),
                user.getEmail(),
                passwordEncoder.encode(rawPassword),
                user.getDisplayName(),
                user.getAvatarUrl(),
                user.isActive(),
                user.getBanReason(),
                user.getBannedAt(),
                user.getLastLoginAt(),
                user.getRoles(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    private String generateOtp() {
        return String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));
    }

    private long toExpirationMinutes(long expirationMs) {
        return Math.max(1L, (expirationMs + 59_999L) / 60_000L);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashedToken = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashedToken);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 algorithm is not available", ex);
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeDisplayName(String displayName) {
        return displayName.trim();
    }
}
