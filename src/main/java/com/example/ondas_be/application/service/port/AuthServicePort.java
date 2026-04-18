package com.example.ondas_be.application.service.port;

import com.example.ondas_be.application.dto.request.LoginRequest;
import com.example.ondas_be.application.dto.request.LogoutRequest;
import com.example.ondas_be.application.dto.request.ForgotPasswordRequest;
import com.example.ondas_be.application.dto.request.RegisterRequest;
import com.example.ondas_be.application.dto.request.RefreshTokenRequest;
import com.example.ondas_be.application.dto.request.ResetPasswordRequest;
import com.example.ondas_be.application.dto.response.AuthResponse;

public interface AuthServicePort {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(RefreshTokenRequest request);

    void logout(LogoutRequest request);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);
}
