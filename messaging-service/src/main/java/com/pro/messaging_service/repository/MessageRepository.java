package com.pro.messaging_service.repository;



import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import com.pro.messaging_service.entity.Message;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByConversationIdOrderByTimestampAsc(Long conversationId);
}