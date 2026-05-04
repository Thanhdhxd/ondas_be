package com.example.ondas_be.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class SearchHistory {

    private Long id;
    private UUID userId;
    private String query;
    private LocalDateTime searchedAt;
}
