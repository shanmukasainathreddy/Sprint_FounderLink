package com.pro.messaging_service.dto;



import lombok.Data;

@Data
public class ConversationRequest {
    private Long user1Id;
    private Long user2Id;
}