package com.pro.messaging_service.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pro.messaging_service.dto.ConversationRequest;
import com.pro.messaging_service.dto.MessageRequest;
import com.pro.messaging_service.entity.Conversation;
import com.pro.messaging_service.entity.Message;
import com.pro.messaging_service.service.MessagingService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessagingController {

    private final MessagingService service;

    @PostMapping("/conversation")
    @PreAuthorize("hasAnyRole('FOUNDER','INVESTOR','COFOUNDER','ADMIN')")
    public Conversation createConversation(@RequestBody ConversationRequest request) {
        return service.createConversation(request);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('FOUNDER','INVESTOR','COFOUNDER','ADMIN')")
    public Message sendMessage(@RequestBody MessageRequest request) {
        return service.sendMessage(request);
    }

    @GetMapping("/conversation/{id}")
    @PreAuthorize("hasAnyRole('FOUNDER','INVESTOR','COFOUNDER','ADMIN')")
    public List<Message> getMessages(@PathVariable Long id) {
        return service.getMessages(id);
    }

    @GetMapping("/user/{id}/conversations")
    @PreAuthorize("hasAnyRole('FOUNDER','INVESTOR','COFOUNDER','ADMIN')")
    public List<Conversation> getConversations(@PathVariable Long id) {
        return service.getConversations(id);
    }
}
