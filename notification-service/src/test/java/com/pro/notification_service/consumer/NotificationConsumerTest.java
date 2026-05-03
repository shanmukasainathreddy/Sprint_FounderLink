package com.pro.notification_service.consumer;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;

import com.pro.notification_service.dto.NotificationEvent;
import com.pro.notification_service.service.NotificationService;

@ExtendWith(MockitoExtension.class)
class NotificationConsumerTest {

    @Mock
    private NotificationService service;

    @InjectMocks
    private NotificationConsumer consumer;

    @Test
    void testConsumeSuccess() {
        NotificationEvent event = new NotificationEvent();
        event.setEmail("test@gmail.com");
        event.setMessage("Test message");

        consumer.consume(event);

        verify(service, times(1)).sendEmail("test@gmail.com", "Test message");
    }

    @Test
    void testConsumeException() {
        NotificationEvent event = new NotificationEvent();
        event.setEmail("error@gmail.com");
        event.setMessage("Error message");

        doThrow(new RuntimeException("Service Error")).when(service).sendEmail(any(), any());

        assertThrows(AmqpRejectAndDontRequeueException.class, () -> consumer.consume(event));
        verify(service, times(1)).sendEmail("error@gmail.com", "Error message");
    }
}
