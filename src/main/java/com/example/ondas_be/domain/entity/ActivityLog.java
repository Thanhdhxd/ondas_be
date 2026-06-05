package com.example.ondas_be.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class ActivityLog {

    private Long id;
    private UUID actorId;
    private String action;
    private String resourceType;
    private UUID resourceId;
    private String resourceName;
    private String metadata;
    private String ipAddress;
    private LocalDateTime createdAt;
}
