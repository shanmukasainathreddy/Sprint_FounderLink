package com.capgemini.springbootbasic;

public class Engine {
	private String fuel_type;
	private double cc;
	
	Engine(){}

	public Engine(String fuel_type, double cc) {
		super();
		this.fuel_type = fuel_type;
		this.cc = cc;
	}

	public String getFuel_type() {
		return fuel_type;
	}

	public void setFuel_type(String fuel_type) {
		this.fuel_type = fuel_type;
	}

	public double getCc() {
		return cc;
	}

	public void setCc(double cc) {
		this.cc = cc;
	}

	@Override
	public String toString() {
		return "Engine [fuel_type=" + fuel_type + ", cc=" + cc + "]";
	}
	
	
	
}
