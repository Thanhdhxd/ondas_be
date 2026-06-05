package com.example.ondas_be.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminActivityLogResponse {
    private Long id;
    private UUID actorId;
    private String actorEmail;
    private String actorDisplayName;
    private String action;
    private String resourceType;
    private UUID resourceId;
    private String resourceName;
    private String metadata;
    private String ipAddress;
    private LocalDateTime createdAt;
}
