package com.example.ondas_be.infrastructure.persistence.model;

import com.example.ondas_be.domain.entity.Favorite;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "favorites")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteModel {

    @EmbeddedId
    private FavoriteId id;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    public Favorite toDomain() {
        return new Favorite(id.getUserId(), id.getSongId(), createdAt);
    }

    public static FavoriteModel fromDomain(UUID userId, UUID songId) {
        return FavoriteModel.builder()
                .id(new FavoriteId(userId, songId))
                .build();
    }
}
