package com.pro.messaging_service.dto;



import lombok.Data;

@Data
public class NotificationEvent {
    private String email;
    private String message;
}