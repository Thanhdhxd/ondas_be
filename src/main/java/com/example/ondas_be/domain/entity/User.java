package com.example.ondas_be.domain.entity;

import java.time.LocalDateTime;
import java.util.Set;
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
    private final Set<Role> roles;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public User(UUID id, String email, String passwordHash, String displayName,
            String avatarUrl,
            boolean active, String banReason,
            LocalDateTime bannedAt, LocalDateTime lastLoginAt,
            Set<Role> roles, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
        this.avatarUrl = avatarUrl;
        this.active = active;
        this.banReason = banReason;
        this.bannedAt = bannedAt;
        this.lastLoginAt = lastLoginAt;
        this.roles = roles;
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

    public Set<Role> getRoles() {
        return roles;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public boolean hasRole(Role role) {
        return roles != null && roles.contains(role);
    }

    public boolean isBanned() {
        return bannedAt != null;
    }
}
