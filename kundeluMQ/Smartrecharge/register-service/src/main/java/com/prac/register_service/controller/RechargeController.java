package com.prac.register_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.prac.register_service.model.Recharge;
import com.prac.register_service.service.RechargeProducer;

@RestController
@RequestMapping("/api/recharges")
public class RechargeController {

	@Autowired
	RechargeProducer producer;

	@PostMapping
	public String recharge(@RequestBody Recharge recharge) {

		producer.sendRecharge(recharge);

		return "Recharge Request Sent";

	}

}