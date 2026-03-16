package com.prac.customer_service.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class Customer {

	@Id
	@GeneratedValue
	private Long customerId;

	private String customerName;

	private String email;

	private String mobileNumber;

	private String city;

	public Long getCustomerId() {
		return customerId;
	}

	public void setCustomerId(Long customerId) {
		this.customerId = customerId;
	}

	public String getCustomerName() {
		return customerName;
	}

	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getMobileNumber() {
		return mobileNumber;
	}

	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public Customer() {
		
	}

	public Customer(Long customerId, String customerName, String email, String mobileNumber, String city) {
		
		this.customerId = customerId;
		this.customerName = customerName;
		this.email = email;
		this.mobileNumber = mobileNumber;
		this.city = city;
	}
	
	
	

}
