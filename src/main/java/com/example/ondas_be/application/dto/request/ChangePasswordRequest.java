package com.example.ondas_be.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequest {

    @NotBlank(message = "validation.not_blank")
    private String currentPassword;

    @NotBlank(message = "validation.not_blank")
    @Size(min = 8, message = "validation.size.min")
    private String newPassword;
}
