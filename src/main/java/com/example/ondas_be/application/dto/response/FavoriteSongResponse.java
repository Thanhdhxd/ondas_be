package com.example.ondas_be.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteSongResponse {

    private UUID songId;
    private String title;
    private String slug;
    private Integer durationSeconds;
    private String audioUrl;
    private String audioFormat;
    private String coverUrl;
    private Long playCount;
    private LocalDateTime favoritedAt;
    private List<ArtistSummaryResponse> artists;
    private List<GenreSummaryResponse> genres;
}
