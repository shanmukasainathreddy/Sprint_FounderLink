package com.prac.order_service.messaging;



import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrderProducer {

    @Autowired
    private RabbitTemplate template;

    public void sendOrder(Long bookId, int quantity) {

        String message = bookId + "," + quantity;

        template.convertAndSend("orderExchange", "orderRouting", message);

        System.out.println("Order sent to queue");
    }
}
