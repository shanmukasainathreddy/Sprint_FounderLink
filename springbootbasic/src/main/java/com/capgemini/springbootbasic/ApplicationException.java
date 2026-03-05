package com.capgemini.springbootbasic;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// to handle global exception

@RestControllerAdvice
public class ApplicationException {

	@ExceptionHandler(Exception.class)
	public String handleException() {
		return "Exception Handle Method";
	}
}
