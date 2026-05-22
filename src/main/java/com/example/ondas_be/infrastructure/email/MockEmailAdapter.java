package com.example.ondas_be.infrastructure.email;

import com.example.ondas_be.application.service.port.EmailPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("e2e")
public class MockEmailAdapter implements EmailPort {

    @Override
    public void sendPasswordResetOtp(String to, String displayName, String otp, long expiresInMinutes) {
        String safeName = displayName != null ? displayName : "User";
        log.info("E2E mock email -> to: {}, name: {}, otp: {}, expires: {}m", to, safeName, otp, expiresInMinutes);
    }
}
