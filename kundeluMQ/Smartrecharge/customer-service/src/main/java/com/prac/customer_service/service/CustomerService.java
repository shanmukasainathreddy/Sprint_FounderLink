package com.prac.customer_service.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.prac.customer_service.entity.Customer;
import com.prac.customer_service.repository.CustomerRepository;

@Service
public class CustomerService {

	@Autowired
	private CustomerRepository repository;

	public Customer save(Customer c) {

		return repository.save(c);

	}

	public List<Customer> getAll() {

		return repository.findAll();

	}

}
