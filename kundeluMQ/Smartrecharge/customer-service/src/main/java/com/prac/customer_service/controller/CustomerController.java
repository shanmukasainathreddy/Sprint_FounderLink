package com.prac.customer_service.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.prac.customer_service.entity.Customer;
import com.prac.customer_service.service.CustomerService;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

	@Autowired
	CustomerService service;

	@PostMapping
	public Customer saveCustomer(@RequestBody Customer c) {

		return service.save(c);

	}

	@GetMapping
	public List<Customer> getCustomers() {

		return service.getAll();

	}

}
