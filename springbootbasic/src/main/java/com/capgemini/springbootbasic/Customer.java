package com.capgemini.springbootbasic;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "customers")
public class Customer {

	@Id
	private Long id;
	private String phone;
	private String email;
	private String name;
	private Integer age;
	private String gender;
	private String dob;
	
	
	public Customer() {}


	public Customer(Long id, String phone, String email, String name, Integer age, String gender, String dob) {
		super();
		this.id = id;
		this.phone = phone;
		this.email = email;
		this.name = name;
		this.age = age;
		this.gender = gender;
		this.dob = dob;
	}


	public Long getId() {
		return id;
	}


	public void setId(Long id) {
		this.id = id;
	}


	public String getPhone() {
		return phone;
	}


	public void setPhone(String phone) {
		this.phone = phone;
	}


	public String getEmail() {
		return email;
	}


	public void setEmail(String email) {
		this.email = email;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public Integer getAge() {
		return age;
	}


	public void setAge(Integer age) {
		this.age = age;
	}


	public String getGender() {
		return gender;
	}


	public void setGender(String gender) {
		this.gender = gender;
	}


	public String getDob() {
		return dob;
	}


	public void setDob(String dob) {
		this.dob = dob;
	}


	@Override
	public String toString() {
		return "Customer [id=" + id + ", phone=" + phone + ", email=" + email + ", name=" + name + ", age=" + age
				+ ", gender=" + gender + ", dob=" + dob + "]";
	}
	
	
	
	

}