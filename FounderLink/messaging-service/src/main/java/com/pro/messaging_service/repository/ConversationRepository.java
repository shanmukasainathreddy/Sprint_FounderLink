package com.pro.messaging_service.repository;



import org.springframework.data.jpa.repository.JpaRepository;
import com.pro.messaging_service.entity.Conversation;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
}