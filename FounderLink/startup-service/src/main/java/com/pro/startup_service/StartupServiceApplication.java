package com.pro.startup_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class StartupServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(StartupServiceApplication.class, args);
	}

}
