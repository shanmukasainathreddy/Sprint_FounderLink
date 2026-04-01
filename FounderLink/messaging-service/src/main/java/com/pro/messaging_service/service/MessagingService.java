package com.pro.messaging_service.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

import com.pro.messaging_service.client.UserServiceClient;
import com.pro.messaging_service.dto.ConversationRequest;
import com.pro.messaging_service.dto.MessageRequest;
import com.pro.messaging_service.dto.NotificationEvent;
import com.pro.messaging_service.dto.UserSummaryResponse;
import com.pro.messaging_service.entity.Conversation;
import com.pro.messaging_service.entity.Message;
import com.pro.messaging_service.producer.NotificationProducer;
import com.pro.messaging_service.repository.ConversationRepository;
import com.pro.messaging_service.repository.MessageRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessagingService {

    private final ConversationRepository conversationRepo;
    private final MessageRepository messageRepo;
    private final NotificationProducer producer;
    private final UserServiceClient userServiceClient;

    public Conversation createConversation(ConversationRequest request) {
        Conversation convo = new Conversation();
        convo.setUser1Id(request.getUser1Id());
        convo.setUser2Id(request.getUser2Id());
        Conversation savedConversation = conversationRepo.save(convo);
        log.info("Created conversation with id={}", savedConversation.getId());
        return savedConversation;
    }

    public Message sendMessage(MessageRequest request) {
        Conversation conversation = conversationRepo.findById(request.getConversationId())
                .orElseThrow(() -> new NoSuchElementException(
                        "Conversation not found for id " + request.getConversationId()));

        Long recipientId = resolveRecipientId(conversation, request.getSenderId());

        Message msg = new Message();
        msg.setConversationId(request.getConversationId());
        msg.setSenderId(request.getSenderId());
        msg.setContent(request.getContent());
        msg.setTimestamp(LocalDateTime.now());

        Message saved = messageRepo.save(msg);

        UserSummaryResponse recipient = userServiceClient.getUserById(recipientId);
        NotificationEvent event = new NotificationEvent();
        event.setEmail(recipient.getEmail());
        event.setMessage("New message: " + request.getContent());
        producer.sendNotification(event);

        log.info("Stored message id={} for conversation={}", saved.getId(), request.getConversationId());
        return saved;
    }

    public List<Message> getMessages(Long conversationId) {
        return messageRepo.findByConversationIdOrderByTimestampAsc(conversationId);
    }

    private Long resolveRecipientId(Conversation conversation, Long senderId) {
        if (conversation.getUser1Id().equals(senderId)) {
            return conversation.getUser2Id();
        }
        if (conversation.getUser2Id().equals(senderId)) {
            return conversation.getUser1Id();
        }
        throw new IllegalArgumentException("Sender does not belong to conversation " + conversation.getId());
    }
}
