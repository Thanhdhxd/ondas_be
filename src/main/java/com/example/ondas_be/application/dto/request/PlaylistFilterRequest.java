package com.example.ondas_be.application.dto.request;

import lombok.Data;

@Data
public class PlaylistFilterRequest {

    private String query;
    private Boolean owner = false;
    private Boolean isPublic;
    private int page = 0;
    private int size = 20;
}
