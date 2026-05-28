package com.example.ondas_be.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "validation.not_blank")
    @Email(message = "validation.email")
    @Size(max = 255, message = "validation.size.max")
    private String email;

    @NotBlank(message = "validation.not_blank")
    private String password;

    public void setEmail(String email) {
        this.email = email == null ? null : email.trim();
    }
}