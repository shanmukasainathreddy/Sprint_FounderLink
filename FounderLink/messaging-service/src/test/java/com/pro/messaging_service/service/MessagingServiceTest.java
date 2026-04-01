package com.pro.messaging_service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pro.messaging_service.client.UserServiceClient;
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

    @Test
    void testSendMessage() {
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
        recipient.setEmail("recipient@test.com");

        when(conversationRepository.findById(10L)).thenReturn(Optional.of(conversation));
        when(messageRepository.save(any(Message.class))).thenReturn(savedMessage);
        when(userServiceClient.getUserById(2L)).thenReturn(recipient);
        doNothing().when(notificationProducer).sendNotification(any());

        Message result = messagingService.sendMessage(request);

        assertNotNull(result);
        assertEquals("Hello", result.getContent());
        verify(conversationRepository, times(1)).findById(10L);
        verify(messageRepository, times(1)).save(any(Message.class));
        verify(userServiceClient, times(1)).getUserById(2L);
        verify(notificationProducer, times(1)).sendNotification(any());
    }
}
