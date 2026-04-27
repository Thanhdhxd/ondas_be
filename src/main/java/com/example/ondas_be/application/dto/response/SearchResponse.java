package com.example.ondas_be.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResponse {

    private String query;
    private int page;
    private int size;
    private long totalSongs;
    private long totalArtists;
    private long totalAlbums;
    private List<SongResponse> songs;
    private List<ArtistResponse> artists;
    private List<AlbumResponse> albums;
}
