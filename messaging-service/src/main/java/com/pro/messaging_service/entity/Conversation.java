package com.pro.messaging_service.entity;



import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long user1Id;
    private Long user2Id;
}
