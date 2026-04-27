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
public class AlbumResponse {

    private UUID id;
    private String title;
    private String slug;
    private String coverUrl;
    private LocalDate releaseDate;
    private String albumType;
    private String description;
    private Integer totalTracks;
    private List<UUID> artistIds;
    private List<ArtistSummaryResponse> artists;
    private List<SongSummaryResponse> tracklist;
}
