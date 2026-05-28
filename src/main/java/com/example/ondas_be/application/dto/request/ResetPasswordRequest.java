package com.example.ondas_be.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequest {

    @NotBlank(message = "validation.not_blank")
    @Email(message = "validation.email")
    @Size(max = 255, message = "validation.size.max")
    private String email;

    @NotBlank(message = "validation.not_blank")
    @Pattern(regexp = "^\\d{6}$", message = "validation.pattern")
    private String otp;

    @NotBlank(message = "validation.not_blank")
    @Size(min = 8, message = "validation.size.min")
    private String newPassword;
}
