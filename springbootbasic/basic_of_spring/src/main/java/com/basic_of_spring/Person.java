package com.basic_of_spring;

import java.util.ArrayList;
import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Person {
	
	//Dependency Injection -> it will inject reference of mobile in person class
	@Autowired
	private Mobile mobile;// null | reference 
	
	@Autowired
	private Scanner scan; // null
	
	@Autowired
	ArrayList<String> item;
	
	
	public ArrayList<String> getItem() {
		return item;
	}

	public void setItem(ArrayList<String> item) {
		this.item = item;
	}

	public Scanner getScan() {
		return scan;
	}

	public void setScan(Scanner scan) {
		this.scan = scan;
	}

	public Mobile getMobile() {
		return mobile;
	}

	public void setMobile(Mobile mobile) {
		this.mobile = mobile;
	}

	public void message() {
		System.out.println("Hi");
	}
	
}
