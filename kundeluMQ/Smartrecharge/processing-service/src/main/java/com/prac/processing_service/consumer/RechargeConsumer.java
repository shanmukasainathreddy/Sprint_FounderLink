package com.prac.processing_service.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import com.prac.processing_service.model.Recharge;

@Service
public class RechargeConsumer {

	@RabbitListener(queues = "rechargeQueue")

	public void processRecharge(Recharge recharge) {

		System.out.println("Processing recharge for " + recharge.getMobileNumber());

	}

}
