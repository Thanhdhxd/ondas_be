package com.example.ondas_be.infrastructure.persistence.model;

import com.example.ondas_be.domain.entity.Role;
import com.example.ondas_be.domain.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserModel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "ban_reason")
    private String banReason;

    @Column(name = "banned_at")
    private LocalDateTime bannedAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    @Builder.Default
    private Role role = Role.USER;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public User toDomain() {
        return new User(id, email, passwordHash, displayName, avatarUrl,
                active,
                banReason, bannedAt, lastLoginAt,
                role,
                createdAt, updatedAt);
    }

    public static UserModel fromDomain(User user) {
        return UserModel.builder()
                .id(user.getId())
                .email(user.getEmail())
                .passwordHash(user.getPasswordHash())
                .displayName(user.getDisplayName())
                .avatarUrl(user.getAvatarUrl())
                .active(user.isActive())
                .banReason(user.getBanReason())
                .bannedAt(user.getBannedAt())
                .lastLoginAt(user.getLastLoginAt())
                .role(user.getRole() != null ? user.getRole() : Role.USER)
                .build();
    }
}
