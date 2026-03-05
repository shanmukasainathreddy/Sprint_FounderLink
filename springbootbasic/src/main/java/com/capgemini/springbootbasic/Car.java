package com.capgemini.springbootbasic;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Car {
	
	@Id
	private int id;
	
	private String brand;
	private double price;
//	private Engine e;
	
	@Override
	public String toString() {
		return "Car [brand=" + brand + ", price=" + price + "";
	}

	public Car(String brand, double price) {
		super();
		this.brand = brand;
		this.price = price;
	}
	
	
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getBrand() {
		return brand;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public Car() {}
	
}