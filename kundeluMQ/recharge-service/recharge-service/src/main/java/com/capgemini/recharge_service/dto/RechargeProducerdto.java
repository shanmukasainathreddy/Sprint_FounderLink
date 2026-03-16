package com.capgemini.recharge_service.dto;

import java.io.Serializable;

public class RechargeProducerdto implements Serializable{
	
	private String mobilenumber;
	private double amount;
	private String operator;
	public String getMobilenumber() {
		return mobilenumber;
	}
	public void setMobilenumber(String mobilenumber) {
		this.mobilenumber = mobilenumber;
	}
	public double getAmount() {
		return amount;
	}
	public void setAmount(double amount) {
		this.amount = amount;
	}
	public String getOperator() {
		return operator;
	}
	public void setOperator(String operator) {
		this.operator = operator;
	}
	public RechargeProducerdto() {
		
	}
	public RechargeProducerdto(String mobilenumber, double amount, String operator) {
		
		this.mobilenumber = mobilenumber;
		this.amount = amount;
		this.operator = operator;
	}
	
	
	
	
	

}
