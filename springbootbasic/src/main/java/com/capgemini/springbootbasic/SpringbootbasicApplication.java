package com.capgemini.springbootbasic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
//@Configuration
//@ComponentScan(basePackages="com")
//@EnableAutoConfiguration
public class SpringbootbasicApplication {

	public static void main(String[] args) {
		ApplicationContext ioc = SpringApplication.run(SpringbootbasicApplication.class, args);
		
		
		
		Doctor doc = ioc.getBean(Doctor.class);
		doc.check();
	}

}
