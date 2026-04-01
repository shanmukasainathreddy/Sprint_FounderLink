package com.pro.messaging_service.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.pro.messaging_service.service.MessagingService;

@WebMvcTest(MessagingController.class)
class MessagingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MessagingService service;

    @Test
    @WithMockUser(authorities = "ROLE_FOUNDER")
    void testSendMessage() throws Exception {
        mockMvc.perform(post("/messages")
                .with(csrf())  
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\":\"Hello\"}"))
                .andExpect(status().isOk());
    }
}