package com.ampgo.demo.service.impl;

import com.ampgo.demo.service.NotificationService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailNotificationServiceImpl implements NotificationService {

    private final JavaMailSender mailSender;

    @Value("${mail.from}")
    private String mailFrom;

    @Value("${mail.from-name}")
    private String mailFromName;


    @Retryable(
            retryFor = {Exception.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2))
    @Override
    public void send(String email, String verificationCode) {
        try {
            log.info("Preparing to send verification email to: {}", email);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(mailFrom, mailFromName);
            helper.setTo(email);
            helper.setSubject("Email Verification Code - AmpGo Demo");
            helper.setText(buildEmailContent(verificationCode), true);
            mailSender.send(message);
            log.info("Verification email sent successfully to: {}", email);
        } catch (MessagingException e) {
            log.error("Failed to send verification email to: {}", email, e);
            throw new RuntimeException("Failed to send verification email", e);
        } catch (Exception e) {
            log.error("Unexpected error while sending email to: {}", email, e);
            throw new RuntimeException("Unexpected error while sending email", e);
        }
    }

    private String buildEmailContent(String verificationCode) throws IOException {
        ClassPathResource resource = new ClassPathResource("template/verification-email.html");
        try (InputStream inputStream = resource.getInputStream()) {
            String template = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            return template.replace("{{code}}", verificationCode);
        }
    }
}