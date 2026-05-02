package com.example.ondas_be.infrastructure.persistence.model;

import com.example.ondas_be.domain.entity.Lyrics;
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
@Table(name = "lyrics")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LyricsModel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "song_id", nullable = false, unique = true)
    private UUID songId;

    @Column(name = "plain_text", columnDefinition = "TEXT")
    private String plainText;

    @Column(name = "has_synced", nullable = false)
    @Builder.Default
    private boolean hasSynced = false;

    @Column(name = "created_by")
    private UUID createdBy;

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

    public Lyrics toDomain() {
        return new Lyrics(id, songId, plainText, hasSynced, createdAt, updatedAt, createdBy);
    }

    public static LyricsModel fromDomain(Lyrics lyrics) {
        return LyricsModel.builder()
                .id(lyrics.getId())
                .songId(lyrics.getSongId())
                .plainText(lyrics.getPlainText())
                .hasSynced(lyrics.isHasSynced())
                .createdBy(lyrics.getCreatedBy())
                // createdAt và updatedAt sẽ được map thủ công hoặc qua lifecycle methods
                .build();
    }
}
