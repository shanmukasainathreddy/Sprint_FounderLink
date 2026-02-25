package com.capgemini.springbootbasic;

import org.springframework.stereotype.Component;

@Component
public class Doctor {
	public void check() {
		System.out.println("Checked");
	}
}
