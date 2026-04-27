package com.example.ondas_be.application.dto.request;

import lombok.Data;

@Data
public class SearchFilterRequest {

    private String query;
    private int page = 0;
    private int size = 10;
}
