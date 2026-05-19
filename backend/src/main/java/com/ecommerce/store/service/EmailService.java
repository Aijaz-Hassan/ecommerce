package com.ecommerce.store.service;

import com.ecommerce.store.entity.User;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final String mailUsername;

    public EmailService(
        ObjectProvider<JavaMailSender> mailSenderProvider,
        @Value("${spring.mail.username:}") String mailUsername
    ) {
        this.mailSenderProvider = mailSenderProvider;
        this.mailUsername = mailUsername;
    }

    public void sendPasswordResetEmail(User user, String resetUrl, int expirationMinutes) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null || mailUsername == null || mailUsername.isBlank()) {
            System.out.println("Password reset link for " + user.getEmail() + ": " + resetUrl);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(user.getEmail());
            helper.setSubject("Reset your Lumen Lane password");
            helper.setFrom(mailUsername);
            helper.setText(buildResetEmail(user, resetUrl, expirationMinutes), true);
            mailSender.send(message);
        } catch (Exception exception) {
            System.out.println("Password reset email fallback for " + user.getEmail() + ": " + resetUrl);
        }
    }

    private String buildResetEmail(User user, String resetUrl, int expirationMinutes) {
        return """
            <div style="font-family:Arial,sans-serif;background:#f6f2eb;padding:32px;color:#231815">
              <div style="max-width:560px;margin:auto;background:#fffaf4;border-radius:20px;padding:28px;box-shadow:0 18px 50px rgba(35,24,21,.12)">
                <h1 style="margin:0 0 12px;font-size:28px">Reset your password</h1>
                <p style="line-height:1.6">Hi %s, we received a request to reset your Lumen Lane password.</p>
                <a href="%s" style="display:inline-block;margin:18px 0;padding:14px 22px;border-radius:999px;background:#e85d3f;color:#fff;text-decoration:none;font-weight:700">Reset Password</a>
                <p style="line-height:1.6">This secure link expires in %d minutes. If you did not request this, you can safely ignore this email.</p>
              </div>
            </div>
            """.formatted(user.getFullName(), resetUrl, expirationMinutes);
    }
}
