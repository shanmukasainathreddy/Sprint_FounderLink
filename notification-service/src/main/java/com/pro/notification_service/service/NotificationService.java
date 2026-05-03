package com.pro.notification_service.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final JavaMailSender mailSender;
    @Value("${spring.mail.username:}")
    private String mailUsername;
    @Value("${spring.mail.password:}")
    private String mailPassword;

    public void sendEmail(String to, String message) {
        validateMailConfiguration();

        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom(mailUsername);
        mail.setTo(to);
        mail.setSubject("FounderLink Notification");
        mail.setText(message);

        mailSender.send(mail);
        log.info("Email sent to {}", to);
    }

    private void validateMailConfiguration() {
        if (!StringUtils.hasText(mailUsername) || !StringUtils.hasText(mailPassword)) {
            throw new IllegalStateException(
                    "SMTP credentials are missing. Set SPRING_MAIL_USERNAME and SPRING_MAIL_PASSWORD.");
        }
    }
}
