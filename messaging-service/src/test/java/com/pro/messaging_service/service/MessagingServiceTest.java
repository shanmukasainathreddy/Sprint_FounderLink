package com.pro.messaging_service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.pro.messaging_service.client.UserServiceClient;
import com.pro.messaging_service.dto.ConversationRequest;
import com.pro.messaging_service.dto.MessageRequest;
import com.pro.messaging_service.dto.UserSummaryResponse;
import com.pro.messaging_service.entity.Conversation;
import com.pro.messaging_service.entity.Message;
import com.pro.messaging_service.producer.NotificationProducer;
import com.pro.messaging_service.repository.ConversationRepository;
import com.pro.messaging_service.repository.MessageRepository;

@ExtendWith(MockitoExtension.class)
class MessagingServiceTest {

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private NotificationProducer notificationProducer;

    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private MessagingService messagingService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testSendMessage() {
        authenticateAsUser(1L);
        MessageRequest request = new MessageRequest();
        request.setConversationId(10L);
        request.setSenderId(1L);
        request.setContent("Hello");

        Conversation conversation = new Conversation();
        conversation.setId(10L);
        conversation.setUser1Id(1L);
        conversation.setUser2Id(2L);

        Message savedMessage = new Message();
        savedMessage.setId(100L);
        savedMessage.setContent("Hello");

        UserSummaryResponse recipient = new UserSummaryResponse();
        recipient.setId(2L);
        recipient.setName("Recipient");
        recipient.setEmail("recipient@test.com");
        UserSummaryResponse sender = new UserSummaryResponse();
        sender.setId(1L);
        sender.setName("Sender");
        sender.setEmail("sender@test.com");

        when(conversationRepository.findById(10L)).thenReturn(Optional.of(conversation));
        when(messageRepository.save(any(Message.class))).thenReturn(savedMessage);
        when(userServiceClient.getUserById(1L)).thenReturn(sender);
        when(userServiceClient.getUserById(2L)).thenReturn(recipient);
        doNothing().when(notificationProducer).sendNotification(any());

        Message result = messagingService.sendMessage(request);

        assertNotNull(result);
        assertEquals("Hello", result.getContent());
        verify(conversationRepository, times(1)).findById(10L);
        verify(messageRepository, times(1)).save(any(Message.class));
        verify(userServiceClient, times(1)).getUserById(1L);
        verify(userServiceClient, times(1)).getUserById(2L);
        verify(notificationProducer, times(1)).sendNotification(argThat(event ->
                "recipient@test.com".equals(event.getEmail())
                        && event.getMessage().contains("Sender <sender@test.com>")
                        && event.getMessage().contains("Hello")));
    }

    @Test
    void testCreateConversationCreatesWhenAuthenticatedParticipant() {
        authenticateAsUser(1L);
        ConversationRequest request = new ConversationRequest();
        request.setUser1Id(1L);
        request.setUser2Id(2L);

        Conversation saved = new Conversation();
        saved.setId(10L);
        saved.setUser1Id(1L);
        saved.setUser2Id(2L);

        when(conversationRepository.findByUser1IdAndUser2Id(1L, 2L)).thenReturn(Optional.empty());
        when(conversationRepository.findByUser1IdAndUser2Id(2L, 1L)).thenReturn(Optional.empty());
        when(conversationRepository.save(any(Conversation.class))).thenReturn(saved);

        Conversation result = messagingService.createConversation(request);

        assertEquals(10L, result.getId());
        verify(conversationRepository, times(1)).save(any(Conversation.class));
    }

    @Test
    void testCreateConversationRejectsNonParticipant() {
        authenticateAsUser(3L);
        ConversationRequest request = new ConversationRequest();
        request.setUser1Id(1L);
        request.setUser2Id(2L);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> messagingService.createConversation(request));

        assertEquals("Authenticated user must be a participant in the conversation", ex.getMessage());
        verify(conversationRepository, never()).save(any());
    }

    @Test
    void testSendMessageRejectsWrongSender() {
        authenticateAsUser(9L);
        MessageRequest request = new MessageRequest();
        request.setConversationId(10L);
        request.setSenderId(1L);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> messagingService.sendMessage(request));

        assertEquals("Sender does not match authenticated user", ex.getMessage());
    }

    @Test
    void testSendMessageRejectsMissingConversation() {
        authenticateAsUser(1L);
        MessageRequest request = new MessageRequest();
        request.setConversationId(10L);
        request.setSenderId(1L);

        when(conversationRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> messagingService.sendMessage(request));
    }

    @Test
    void testGetMessagesDelegatesToRepository() {
        Message message = new Message();
        message.setConversationId(10L);
        when(messageRepository.findByConversationIdOrderByTimestampAsc(10L)).thenReturn(List.of(message));

        List<Message> result = messagingService.getMessages(10L);

        assertEquals(1, result.size());
        verify(messageRepository, times(1)).findByConversationIdOrderByTimestampAsc(10L);
    }

    @Test
    void testGetConversationsAllowsAuthenticatedOwner() {
        authenticateAsUser(1L);
        Conversation conversation = new Conversation();
        conversation.setUser1Id(1L);
        when(conversationRepository.findByUser1IdOrUser2Id(1L, 1L)).thenReturn(List.of(conversation));

        List<Conversation> result = messagingService.getConversations(1L);

        assertEquals(1, result.size());
    }

    @Test
    void testGetConversationsRejectsDifferentUser() {
        authenticateAsUser(2L);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> messagingService.getConversations(1L));

        assertEquals("Authenticated user can only view their own conversations", ex.getMessage());
    }

    @Test
    void testRequiresAuthentication() {
        SecurityContextHolder.clearContext();

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> messagingService.getConversations(1L));

        assertEquals("Authentication is required", ex.getMessage());
    }

    private void authenticateAsUser(Long userId) {
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(String.valueOf(userId), null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
