package com.pro.messaging_service.repository;

import com.pro.messaging_service.entity.Message;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class MessageRepositoryTest {

    @Autowired
    private MessageRepository repository;

    @Test
    void testSave() {
        Message message = new Message();
        message.setContent("Test");

        Message saved = repository.save(message);

        assertNotNull(saved.getId());
    }
}