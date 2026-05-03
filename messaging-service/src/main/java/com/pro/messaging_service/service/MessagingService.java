package com.pro.messaging_service.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
        Long requesterId = getAuthenticatedUserId();
        if (!requesterId.equals(request.getUser1Id()) && !requesterId.equals(request.getUser2Id())) {
            throw new IllegalArgumentException("Authenticated user must be a participant in the conversation");
        }

        Conversation savedConversation = conversationRepo.findByUser1IdAndUser2Id(request.getUser1Id(), request.getUser2Id())
                .or(() -> conversationRepo.findByUser1IdAndUser2Id(request.getUser2Id(), request.getUser1Id()))
                .orElseGet(() -> {
                    Conversation convo = new Conversation();
                    convo.setUser1Id(request.getUser1Id());
                    convo.setUser2Id(request.getUser2Id());
                    return conversationRepo.save(convo);
                });
        log.info("Created conversation with id={}", savedConversation.getId());
        return savedConversation;
    }

    public Message sendMessage(MessageRequest request) {
        Long requesterId = getAuthenticatedUserId();
        if (!requesterId.equals(request.getSenderId())) {
            throw new IllegalArgumentException("Sender does not match authenticated user");
        }

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

        notifyRecipient(request, recipientId);

        log.info("Stored message id={} for conversation={}", saved.getId(), request.getConversationId());
        return saved;
    }

    public List<Message> getMessages(Long conversationId) {
        return messageRepo.findByConversationIdOrderByTimestampAsc(conversationId);
    }

    public List<Conversation> getConversations(Long userId) {
        Long requesterId = getAuthenticatedUserId();
        if (!requesterId.equals(userId)) {
            throw new IllegalArgumentException("Authenticated user can only view their own conversations");
        }
        return conversationRepo.findByUser1IdOrUser2Id(userId, userId);
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

    private void notifyRecipient(MessageRequest request, Long recipientId) {
        try {
            UserSummaryResponse sender = userServiceClient.getUserById(request.getSenderId());
            UserSummaryResponse recipient = userServiceClient.getUserById(recipientId);
            if (recipient == null || recipient.getEmail() == null || recipient.getEmail().isBlank()) {
                return;
            }

            String senderName = sender != null && sender.getName() != null && !sender.getName().isBlank()
                    ? sender.getName()
                    : "FounderLink user";
            String senderEmail = sender != null && sender.getEmail() != null && !sender.getEmail().isBlank()
                    ? sender.getEmail()
                    : "unknown email";
            String recipientName = recipient.getName() == null || recipient.getName().isBlank()
                    ? "you"
                    : recipient.getName();

            NotificationEvent event = new NotificationEvent();
            event.setEmail(recipient.getEmail());
            event.setMessage(String.format(
                    "FounderLink message for %s from %s <%s>: %s. Reply inside FounderLink to keep the conversation secure.",
                    recipientName,
                    senderName,
                    senderEmail,
                    request.getContent()));
            producer.sendNotification(event);
        } catch (RuntimeException ex) {
            log.warn("Could not publish message email notification for conversation={}", request.getConversationId(), ex);
        }
    }

    private Long getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalStateException("Authentication is required");
        }

        try {
            return Long.parseLong(authentication.getName());
        } catch (NumberFormatException ex) {
            throw new IllegalStateException("Invalid authenticated user id");
        }
    }
}
