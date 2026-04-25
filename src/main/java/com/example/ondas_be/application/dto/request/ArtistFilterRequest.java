package com.example.ondas_be.application.dto.request;

import lombok.Data;

@Data
public class ArtistFilterRequest {

    private String query;
    private String mode = "contains";
    private int page = 0;
    private int size = 20;
}
