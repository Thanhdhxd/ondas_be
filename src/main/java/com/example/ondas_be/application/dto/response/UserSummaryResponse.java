package com.example.ondas_be.application.dto.response;

import com.example.ondas_be.domain.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryResponse {

    private UUID id;
    private String email;
    private String displayName;
    private Role role;
}
