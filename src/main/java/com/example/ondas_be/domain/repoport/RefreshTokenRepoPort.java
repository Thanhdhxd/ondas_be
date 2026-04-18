package com.example.ondas_be.domain.repoport;

import com.example.ondas_be.domain.entity.RefreshToken;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepoPort {

    RefreshToken save(RefreshToken refreshToken);

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    void revokeAllByUserId(UUID userId);

    void revokeByTokenHash(String tokenHash);
}