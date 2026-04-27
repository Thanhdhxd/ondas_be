package com.example.ondas_be.infrastructure.persistence.model;

import com.example.ondas_be.domain.entity.Playlist;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "playlists")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaylistModel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "cover_url", columnDefinition = "TEXT")
    private String coverUrl;

    @Column(name = "is_public", nullable = false)
    @Builder.Default
    private boolean isPublic = false;

    @Column(name = "total_songs", nullable = false)
    @Builder.Default
    private Integer totalSongs = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        if (this.updatedAt == null) {
            this.updatedAt = now;
        }
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Playlist toDomain() {
        return new Playlist(
                id,
                userId,
                name,
                description,
                coverUrl,
                isPublic,
                totalSongs,
                createdAt,
                updatedAt
        );
    }

    public static PlaylistModel fromDomain(Playlist playlist) {
        return PlaylistModel.builder()
                .id(playlist.getId())
                .userId(playlist.getUserId())
                .name(playlist.getName())
                .description(playlist.getDescription())
                .coverUrl(playlist.getCoverUrl())
                .isPublic(playlist.isPublic())
                .totalSongs(playlist.getTotalSongs())
                .createdAt(playlist.getCreatedAt())
                .updatedAt(playlist.getUpdatedAt())
                .build();
    }
}
