package com.pro.notification_service.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private NotificationService service;

    @Test
    void testSendEmail_success() {
        ReflectionTestUtils.setField(service, "mailUsername", "noreply@founderlink.com");
        ReflectionTestUtils.setField(service, "mailPassword", "secret");

        service.sendEmail("test@gmail.com", "Hello");

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendEmail_failure() {
        ReflectionTestUtils.setField(service, "mailUsername", "noreply@founderlink.com");
        ReflectionTestUtils.setField(service, "mailPassword", "secret");

        doThrow(new RuntimeException("Mail error"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        assertThrows(RuntimeException.class, () ->
                service.sendEmail("test@gmail.com", "Hello"));
    }

    @Test
    void testSendEmail_missingCredentials() {
        ReflectionTestUtils.setField(service, "mailUsername", "");
        ReflectionTestUtils.setField(service, "mailPassword", "");

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                service.sendEmail("test@gmail.com", "Hello"));

        assertTrue(ex.getMessage().contains("SMTP credentials are missing"));
    }
}
