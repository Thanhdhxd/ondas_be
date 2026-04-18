package com.example.ondas_be.application.dto.response;

import com.example.ondas_be.domain.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {

    private UUID id;
    private String email;
    private String displayName;
    private String avatarUrl;
    private Set<Role> roles;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
}
