package com.capgemini.recharge_service.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.capgemini.recharge_service.config.RabbitConfig;
import com.capgemini.recharge_service.dto.RechargeProducerdto;

@Service
public class RechargeService {
	
	private final RabbitTemplate rabbitTemplate;

	public RechargeService(RabbitTemplate rabbitTemplate) {
		this.rabbitTemplate=rabbitTemplate;
	}
	
	
	public String sendRechargeRequest(RechargeProducerdto request) {
		rabbitTemplate.convertAndSend(RabbitConfig.Queue_Name, request);
		return "Message has been send to broker";
	}
	
	

}
