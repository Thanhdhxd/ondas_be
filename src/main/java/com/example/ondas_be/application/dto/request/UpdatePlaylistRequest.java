package com.example.ondas_be.application.dto.request;

import lombok.Data;

@Data
public class UpdatePlaylistRequest {

    private String name;
    private String description;
    private Boolean isPublic;
}
