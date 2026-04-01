package com.pro.notification_service.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notification Controller", description = "Endpoints for notification service status")
public class NotificationController {

    @GetMapping("/status")
    @Operation(summary = "Check service status", description = "Returns the status of the notification service")
    public String getStatus() {
        return "Notification Service is running and consuming messages.";
    }
}
