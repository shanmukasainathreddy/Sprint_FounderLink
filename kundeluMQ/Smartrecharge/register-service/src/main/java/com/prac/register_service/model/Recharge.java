package com.prac.register_service.model;

public class Recharge {

	private Long rechargeId;

	private Long customerId;

	private String mobileNumber;

	private String operatorName;

	private Double amount;

	public Long getRechargeId() {
		return rechargeId;
	}

	public void setRechargeId(Long rechargeId) {
		this.rechargeId = rechargeId;
	}

	public Long getCustomerId() {
		return customerId;
	}

	public void setCustomerId(Long customerId) {
		this.customerId = customerId;
	}

	public String getMobileNumber() {
		return mobileNumber;
	}

	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}

	public String getOperatorName() {
		return operatorName;
	}

	public void setOperatorName(String operatorName) {
		this.operatorName = operatorName;
	}

	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

	public Recharge() {
		
	}

	public Recharge(Long rechargeId, Long customerId, String mobileNumber, String operatorName, Double amount) {
		
		this.rechargeId = rechargeId;
		this.customerId = customerId;
		this.mobileNumber = mobileNumber;
		this.operatorName = operatorName;
		this.amount = amount;
	}
	
	
	
	

}
