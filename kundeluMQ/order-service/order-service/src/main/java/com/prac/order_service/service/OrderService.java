package com.prac.order_service.service;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.prac.order_service.entity.Order;
import com.prac.order_service.messaging.OrderProducer;
import com.prac.order_service.repository.OrderRepository;



@Service
public class OrderService {

    @Autowired
    private OrderRepository repo;

    @Autowired
    private OrderProducer producer;

    public Order createOrder(Order order) {

        Order saved = repo.save(order);

        producer.sendOrder(order.getBookId(), order.getQuantity());

        return saved;
    }
}
