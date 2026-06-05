package com.example.ondas_be.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminTopSongResponse {

    private UUID id;
    private String title;
    private String coverUrl;
    private long playCount;
    private List<ArtistSummaryResponse> artists;
}
