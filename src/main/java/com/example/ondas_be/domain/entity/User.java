package com.example.ondas_be.domain.entity;

import java.time.LocalDateTime;
import java.util.UUID;

// Pure domain entity — KHÔNG có annotation Spring, JPA, Lombok
public class User {

    private final UUID id;
    private final String email;
    private final String passwordHash;
    private final String displayName;
    private final String avatarUrl;
    private final boolean active;
    private final String banReason; // nullable
    private final LocalDateTime bannedAt; // nullable
    private final LocalDateTime lastLoginAt; // nullable
    private final Role role;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public User(UUID id, String email, String passwordHash, String displayName,
            String avatarUrl,
            boolean active, String banReason,
            LocalDateTime bannedAt, LocalDateTime lastLoginAt,
            Role role, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
        this.avatarUrl = avatarUrl;
        this.active = active;
        this.banReason = banReason;
        this.bannedAt = bannedAt;
        this.lastLoginAt = lastLoginAt;
        this.role = role;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public boolean isActive() {
        return active;
    }

    public String getBanReason() {
        return banReason;
    }

    public LocalDateTime getBannedAt() {
        return bannedAt;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public Role getRole() {
        return role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public boolean hasRole(Role role) {
        return this.role == role;
    }

    public boolean isBanned() {
        return bannedAt != null;
    }
}
