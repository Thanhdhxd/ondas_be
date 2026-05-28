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
public class RegisterRequest {

    @NotBlank(message = "validation.not_blank")
    @Email(message = "validation.email")
    @Pattern(regexp = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}$", message = "validation.pattern")
    @Size(max = 255, message = "validation.size.max")
    private String email;

    @NotBlank(message = "validation.not_blank")
    @Size(min = 6, message = "validation.size.min")
    private String password;

    @NotBlank(message = "validation.not_blank")
    @Size(min = 2, max = 100, message = "validation.size.range")
    private String displayName;

    public void setEmail(String email) {
        this.email = email == null ? null : email.trim();
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName == null ? null : displayName.trim();
    }
}
