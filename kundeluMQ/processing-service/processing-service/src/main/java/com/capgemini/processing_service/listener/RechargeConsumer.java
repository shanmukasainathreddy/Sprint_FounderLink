package com.capgemini.processing_service.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.capgemini.processing_service.config.RabbitConfig;
import com.capgemini.processing_service.dto.RechargeProducerdto;

@Component
public class RechargeConsumer {
	
	@RabbitListener(queues = RabbitConfig.Queue_Name)
	public void processRecharge(RechargeProducerdto dto) throws InterruptedException {
		System.out.println("message received from queue");
		System.out.println("recharge processing for mobile "+dto.getMobilenumber());
		
		Thread.sleep(5000);
		System.out.println("Recharge done "+ dto.getMobilenumber());
	}

}
