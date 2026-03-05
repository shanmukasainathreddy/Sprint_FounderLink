package com.capgemini.springbootbasic;


import org.springframework.stereotype.Component;


public class Cricketer {
	
	private String name;
	private String role;
	private int runs;
	private int no_of_matches;
	private double average;
	
	
	public Cricketer() {}


	public Cricketer(String name, String role, int runs, int no_of_matches, double average) {
		super();
		this.name = name;
		this.role = role;
		this.runs = runs;
		this.no_of_matches = no_of_matches;
		this.average = average;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public String getRole() {
		return role;
	}


	public void setRole(String role) {
		this.role = role;
	}


	public int getRuns() {
		return runs;
	}


	public void setRuns(int runs) {
		this.runs = runs;
	}


	public int getNo_of_matches() {
		return no_of_matches;
	}


	public void setNo_of_matches(int no_of_matches) {
		this.no_of_matches = no_of_matches;
	}


	public double getAverage() {
		return average;
	}


	public void setAverage(double average) {
		this.average = average;
	}


	@Override
	public String toString() {
		return "Cricketer [name=" + name + ", role=" + role + ", runs=" + runs + ", no_of_matches=" + no_of_matches
				+ ", average=" + average + "]";
	}
	
	
	
	
	
}