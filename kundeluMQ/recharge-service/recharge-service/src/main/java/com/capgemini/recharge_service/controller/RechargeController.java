package com.capgemini.recharge_service.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.capgemini.recharge_service.dto.RechargeProducerdto;
import com.capgemini.recharge_service.service.RechargeService;


@RestController
public class RechargeController {
	
	private RechargeService service;
	
	public RechargeController(RechargeService service) {
		this.service=service;
	}
	
	
	@PostMapping("/recharge")
	public String doRecharge(@RequestBody RechargeProducerdto dto) {
		return service.sendRechargeRequest(dto);
	}
	

}
