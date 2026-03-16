package com.capgemini.recharge_service.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;

public class RabbitConfig {
	public static final String Queue_Name = "recharge.queue";

	@Bean
	public Queue queue() {
		return new Queue(Queue_Name, true);
	}

}
