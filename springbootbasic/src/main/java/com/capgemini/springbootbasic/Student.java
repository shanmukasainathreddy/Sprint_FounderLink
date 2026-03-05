package com.capgemini.springbootbasic;


public class Student {
	private String name;
	private String branch;
	
	public Student() {}

	public Student(String name, String branch) {
		super();
		this.name = name;
		this.branch = branch;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getBranch() {
		return branch;
	}

	public void setBranch(String branch) {
		this.branch = branch;
	}

	@Override
	public String toString() {
		return "Student [name=" + name + ", branch=" + branch + "]";
	}
	
	
	
}
