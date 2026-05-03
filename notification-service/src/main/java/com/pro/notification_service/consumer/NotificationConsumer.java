package com.pro.notification_service.consumer;

import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.pro.notification_service.config.RabbitConfig;
import com.pro.notification_service.dto.NotificationEvent;
import com.pro.notification_service.service.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationConsumer {

    private final NotificationService service;

    @RabbitListener(queues = RabbitConfig.QUEUE)
    public void consume(NotificationEvent event) {
        log.info("Received notification event for email={}", event.getEmail());
        try {
            service.sendEmail(event.getEmail(), event.getMessage());
            log.info("Notification event processed successfully for email={}", event.getEmail());
        } catch (Exception ex) {
            log.error("Notification delivery failed for email={}: {}", event.getEmail(), ex.getMessage(), ex);
            throw new AmqpRejectAndDontRequeueException("Notification delivery failed", ex);
        }
    }
}
