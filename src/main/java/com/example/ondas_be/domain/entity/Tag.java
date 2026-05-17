package com.example.ondas_be.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class Tag {

    private Long id;
    private String name;
    private String type;
    private String colorHex;
    private LocalDateTime createdAt;
}
