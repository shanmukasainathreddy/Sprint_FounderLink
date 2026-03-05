package com.basic_of_spring;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Main {
	public static void main(String[] args) {
//	ApplicationContext ioc = new ClassPathXmlApplicationContext("config.xml");
//
//	Person person = ioc.getBean("person",Person.class);
//
//	System.out.println(person); //  print reference
//	
//	person.message(); // prints messages from message 
//	
//	Employee e = ioc.getBean("employee",Employee.class);
//	
//	e.setId(101);
//	e.setName("Dingaa");
//	e.setSalary(150000.00);
//	
//	System.out.println(e);
//	System.out.println(e.getId());
//	System.out.println(e.getName());
//	System.out.println(e.getSalary());
		
		
		
		ApplicationContext ioc = new AnnotationConfigApplicationContext(Democonfiguration.class);
//		
//		 Employee e = ioc.getBean(Employee.class);
//		
//		 System.out.println(e);
//		 
		 Person p = ioc.getBean(Person.class);
//		 
		 System.out.println(p);
		 
		 System.out.println(p.getMobile());
		 
		 System.out.println(ioc.getBean(Mobile.class));
		 
		 System.out.println(p.getScan());
		 
		 System.out.println(p.getItem());


		
		
	}
}
