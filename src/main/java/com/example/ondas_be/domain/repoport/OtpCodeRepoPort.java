package com.example.ondas_be.domain.repoport;

import com.example.ondas_be.domain.entity.OtpCode;

import java.util.Optional;
import java.util.UUID;

public interface OtpCodeRepoPort {

    OtpCode save(OtpCode otpCode);

    Optional<OtpCode> findActiveByUserIdAndCodeHash(UUID userId, String codeHash);

    void markAllUnusedByUserId(UUID userId);

    void markAsUsed(UUID id);
}
