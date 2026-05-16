package com.example.ondas_be.application.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class SongTagRequest {

    private List<Long> tagIds;
}
