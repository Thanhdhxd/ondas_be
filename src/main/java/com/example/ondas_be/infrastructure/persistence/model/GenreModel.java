package com.example.ondas_be.infrastructure.persistence.model;

import com.example.ondas_be.domain.entity.Genre;
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

@Entity
@Table(name = "genres")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenreModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "cover_url")
    private String coverUrl;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public Genre toDomain() {
        return new Genre(id, name, slug, description, coverUrl, createdAt);
    }

    public static GenreModel fromDomain(Genre genre) {
        return GenreModel.builder()
                .id(genre.getId())
                .name(genre.getName())
                .slug(genre.getSlug())
                .description(genre.getDescription())
                .coverUrl(genre.getCoverUrl())
                .createdAt(genre.getCreatedAt())
                .build();
    }
}
