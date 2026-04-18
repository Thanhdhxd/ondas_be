package com.example.ondas_be.infrastructure.persistence.model;

import com.example.ondas_be.domain.entity.OtpCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "otp_codes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpCodeModel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "code_hash", nullable = false, length = 255)
    private String codeHash;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "used", nullable = false)
    @Builder.Default
    private boolean used = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public OtpCode toDomain() {
        return new OtpCode(id, userId, codeHash, expiresAt, used, createdAt);
    }

    public static OtpCodeModel fromDomain(OtpCode otpCode) {
        return OtpCodeModel.builder()
                .id(otpCode.getId())
                .userId(otpCode.getUserId())
                .codeHash(otpCode.getCodeHash())
                .expiresAt(otpCode.getExpiresAt())
                .used(otpCode.isUsed())
                .createdAt(otpCode.getCreatedAt())
                .build();
    }
}
