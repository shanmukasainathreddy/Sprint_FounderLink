package com.capgemini.recharge_service.config;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JsonConfig {
	@Bean
	public Jackson2JsonMessageConverter jsonMessage() {
		return new Jackson2JsonMessageConverter();
	}

}
