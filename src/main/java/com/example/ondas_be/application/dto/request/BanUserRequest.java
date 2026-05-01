package com.example.ondas_be.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BanUserRequest {

    @NotBlank(message = "banReason must not be blank")
    private String banReason;
}
