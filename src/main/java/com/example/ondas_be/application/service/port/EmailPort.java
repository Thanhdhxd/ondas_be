package com.example.ondas_be.application.service.port;

public interface EmailPort {

    void sendPasswordResetOtp(String to, String displayName, String otp, long expiresInMinutes);
}
