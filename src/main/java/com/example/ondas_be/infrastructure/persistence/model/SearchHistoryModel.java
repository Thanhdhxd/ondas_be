package com.example.ondas_be.infrastructure.persistence.model;

import com.example.ondas_be.domain.entity.SearchHistory;
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
@Table(name = "search_histories")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchHistoryModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "query", nullable = false, length = 255)
    private String query;

    @Column(name = "searched_at", nullable = false, updatable = false)
    private LocalDateTime searchedAt;

    @PrePersist
    void prePersist() {
        if (this.searchedAt == null) {
            this.searchedAt = LocalDateTime.now();
        }
    }

    public SearchHistory toDomain() {
        return new SearchHistory(id, userId, query, searchedAt);
    }

    public static SearchHistoryModel fromDomain(SearchHistory searchHistory) {
        return SearchHistoryModel.builder()
                .id(searchHistory.getId())
                .userId(searchHistory.getUserId())
                .query(searchHistory.getQuery())
                .searchedAt(searchHistory.getSearchedAt())
                .build();
    }
}
