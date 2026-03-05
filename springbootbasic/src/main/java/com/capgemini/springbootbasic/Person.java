package com.capgemini.springbootbasic;



import java.util.List;

public class Person {
	private int id;
	private String name;
	
	List<String> item;
	
	
	
	public List<String> getItem() {
		return item;
	}

	public void setItem(List<String> item) {
		this.item = item;
	}

	public Person() {}

	public Person(int id, String name, List<String> item) {
		super();
		this.id = id;
		this.name = name;
		this.item=item;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "Person [id=" + id + ", name=" + name + "]";
	}
	
	
	
}
