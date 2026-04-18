package com.example.ondas_be.infrastructure.email;

import com.example.ondas_be.application.service.port.EmailPort;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SmtpEmailAdapter implements EmailPort {

    private final JavaMailSender javaMailSender;

    @Value("${app.mail.from:noreply@ondas.app}")
    private String fromEmail;

    @Override
    public void sendPasswordResetOtp(String to, String displayName, String otp, long expiresInMinutes) {
        MimeMessage message = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Your OTP to reset password");
            helper.setText(buildOtpEmailHtml(displayName, otp, expiresInMinutes), true);
            javaMailSender.send(message);
        } catch (MessagingException ex) {
            throw new IllegalStateException("Unable to send password reset OTP email", ex);
        }
    }

    private String buildOtpEmailHtml(String displayName, String otp, long expiresInMinutes) {
        return """
                <html>
                  <body style=\"font-family: Arial, sans-serif; color: #222;\">
                    <h2>Password reset request</h2>
                    <p>Hello %s,</p>
                    <p>Your OTP code is:</p>
                    <p style=\"font-size: 28px; font-weight: bold; letter-spacing: 6px;\">%s</p>
                    <p>This code will expire in %d minute(s).</p>
                    <p>If you did not request this, please ignore this email.</p>
                  </body>
                </html>
                """.formatted(escapeHtml(displayName), otp, expiresInMinutes);
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "User";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
            .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
