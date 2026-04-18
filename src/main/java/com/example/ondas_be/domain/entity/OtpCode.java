package com.example.ondas_be.domain.entity;

import java.time.LocalDateTime;
import java.util.UUID;

public class OtpCode {

    private final UUID id;
    private final UUID userId;
    private final String codeHash;
    private final LocalDateTime expiresAt;
    private final boolean used;
    private final LocalDateTime createdAt;

    public OtpCode(UUID id, UUID userId, String codeHash, LocalDateTime expiresAt, boolean used, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.codeHash = codeHash;
        this.expiresAt = expiresAt;
        this.used = used;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getCodeHash() {
        return codeHash;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public boolean isUsed() {
        return used;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
