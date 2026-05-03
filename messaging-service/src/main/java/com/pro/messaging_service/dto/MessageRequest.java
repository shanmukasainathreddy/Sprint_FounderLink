package com.pro.messaging_service.dto;



import lombok.Data;

@Data
public class MessageRequest {
    private Long conversationId;
    private Long senderId;
    private String content;
}
