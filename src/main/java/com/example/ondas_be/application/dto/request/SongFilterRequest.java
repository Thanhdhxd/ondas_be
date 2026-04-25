package com.example.ondas_be.application.dto.request;

import lombok.Data;

import java.util.UUID;

@Data
public class SongFilterRequest {

    private String query;
    private String mode = "contains";
    private UUID artistId;
    private UUID albumId;
    private Long genreId;
    private int page = 0;
    private int size = 20;
}
