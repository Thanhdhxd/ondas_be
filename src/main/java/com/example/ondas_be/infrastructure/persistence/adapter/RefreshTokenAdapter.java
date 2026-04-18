package com.example.ondas_be.infrastructure.persistence.adapter;

import com.example.ondas_be.domain.entity.RefreshToken;
import com.example.ondas_be.domain.repoport.RefreshTokenRepoPort;
import com.example.ondas_be.infrastructure.persistence.jparepo.RefreshTokenJpaRepo;
import com.example.ondas_be.infrastructure.persistence.model.RefreshTokenModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RefreshTokenAdapter implements RefreshTokenRepoPort {

    private final RefreshTokenJpaRepo refreshTokenJpaRepo;

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        return refreshTokenJpaRepo.save(RefreshTokenModel.fromDomain(refreshToken)).toDomain();
    }

    @Override
    public Optional<RefreshToken> findByTokenHash(String tokenHash) {
        return refreshTokenJpaRepo.findByTokenHash(tokenHash).map(RefreshTokenModel::toDomain);
    }

    @Override
    @Transactional
    public void revokeAllByUserId(UUID userId) {
        refreshTokenJpaRepo.revokeAllByUserId(userId);
    }

    @Override
    @Transactional
    public void revokeByTokenHash(String tokenHash) {
        refreshTokenJpaRepo.revokeByTokenHash(tokenHash);
    }
}