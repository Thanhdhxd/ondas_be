package com.example.ondas_be.infrastructure.persistence.model;

import com.example.ondas_be.domain.entity.Tag;
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
@Table(name = "tags")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "type", nullable = false, length = 20)
    private String type;

    @Column(name = "color_hex", length = 7)
    private String colorHex;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public Tag toDomain() {
        return new Tag(id, name, type, colorHex, createdAt);
    }

    public static TagModel fromDomain(Tag tag) {
        return TagModel.builder()
                .id(tag.getId())
                .name(tag.getName())
                .type(tag.getType())
                .colorHex(tag.getColorHex())
                .createdAt(tag.getCreatedAt())
                .build();
    }
}
