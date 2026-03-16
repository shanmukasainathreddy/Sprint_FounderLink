package com.prac.register_service.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.prac.register_service.model.Recharge;

@Service
public class RechargeProducer {

	@Autowired
	private RabbitTemplate rabbitTemplate;

	public void sendRecharge(Recharge recharge) {

		rabbitTemplate.convertAndSend("rechargeExchange", "rechargeKey", recharge);

	}

}
