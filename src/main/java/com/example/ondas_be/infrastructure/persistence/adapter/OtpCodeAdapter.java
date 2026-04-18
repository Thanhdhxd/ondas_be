package com.example.ondas_be.infrastructure.persistence.adapter;

import com.example.ondas_be.domain.entity.OtpCode;
import com.example.ondas_be.domain.repoport.OtpCodeRepoPort;
import com.example.ondas_be.infrastructure.persistence.jparepo.OtpCodeJpaRepo;
import com.example.ondas_be.infrastructure.persistence.model.OtpCodeModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OtpCodeAdapter implements OtpCodeRepoPort {

    private final OtpCodeJpaRepo otpCodeJpaRepo;

    @Override
    public OtpCode save(OtpCode otpCode) {
        return otpCodeJpaRepo.save(OtpCodeModel.fromDomain(otpCode)).toDomain();
    }

    @Override
    public Optional<OtpCode> findActiveByUserIdAndCodeHash(UUID userId, String codeHash) {
        return otpCodeJpaRepo.findByUserIdAndCodeHashAndUsedFalse(userId, codeHash)
                .map(OtpCodeModel::toDomain);
    }

    @Override
    @Transactional
    public void markAllUnusedByUserId(UUID userId) {
        otpCodeJpaRepo.markAllUnusedByUserId(userId);
    }

    @Override
    @Transactional
    public void markAsUsed(UUID id) {
        otpCodeJpaRepo.markAsUsed(id);
    }
}
