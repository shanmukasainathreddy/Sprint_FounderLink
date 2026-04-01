package com.pro.messaging_service.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    // ✅ Create conversation
    @PostMapping("/conversation")
    @PreAuthorize("hasAnyRole('FOUNDER','INVESTOR','COFOUNDER')")
    public Conversation createConversation(@RequestBody ConversationRequest request) {
        return service.createConversation(request);
    }

    // ✅ Send message
    @PostMapping
    @PreAuthorize("hasAnyRole('FOUNDER','INVESTOR','COFOUNDER')")
    public Message sendMessage(@RequestBody MessageRequest request) {
        return service.sendMessage(request);
    }

    // ✅ Get messages
    @GetMapping("/conversation/{id}")
    @PreAuthorize("hasAnyRole('FOUNDER','INVESTOR','COFOUNDER')")
    public List<Message> getMessages(@PathVariable Long id) {
        return service.getMessages(id);
    }
}