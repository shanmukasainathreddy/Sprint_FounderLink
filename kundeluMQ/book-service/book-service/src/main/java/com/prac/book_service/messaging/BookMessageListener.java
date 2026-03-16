package com.prac.book_service.messaging;



import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.prac.book_service.service.BookService;



@Component
public class BookMessageListener {

    @Autowired
    private BookService service;

    @RabbitListener(queues = "orderQueue")
    public void receiveMessage(String message) {

        String[] parts = message.split(",");

        Long bookId = Long.parseLong(parts[0]);
        int quantity = Integer.parseInt(parts[1]);

        service.reduceStock(bookId, quantity);

        System.out.println("Stock updated for book " + bookId);
    }
}