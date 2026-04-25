package com.example.ondas_be.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SongResponse {

    private UUID id;
    private String title;
    private String slug;
    private Integer durationSeconds;
    private String audioUrl;
    private String audioFormat;
    private Long audioSizeBytes;
    private String coverUrl;
    private UUID albumId;
    private Integer trackNumber;
    private LocalDate releaseDate;
    private Long playCount;
    private boolean active;
    private List<ArtistSummaryResponse> artists;
    private List<GenreSummaryResponse> genres;
}
