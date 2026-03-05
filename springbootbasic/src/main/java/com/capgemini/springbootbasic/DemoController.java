package com.capgemini.springbootbasic;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {
	
	@Autowired // field injection
	CarJpaRepository cjr;
	
	@PostMapping("/api")
	public Set<String> hello() {
		return Set.of("Miller", "Bravis", "Makram"); // JSON collection
	}
//	
	@PostMapping("/add")
	public String createCricketer(@RequestBody Cricketer c) {
		// take http request json and converts into object
		System.out.println(c);
		return c.toString();
	}
	
	@PostMapping("/car")
	public String createCar(@RequestBody Car c) {
		return cjr.save(c).toString();
	}
//	
	@GetMapping("/id")
	public Car getById(@RequestParam int id) {
	    return cjr.findById(id)
	              .orElseThrow(() -> new RuntimeException("Car not found"));
	}
//	
	@GetMapping("/all")
	public List<Car> getAllCars(){
		return cjr.findAll();
	}
//	
	@DeleteMapping("/delete/{id}")
	public boolean deleteCar(@PathVariable int id) {
		Optional<Car> car = cjr.findById(id);
		if(car.isPresent()) {
			cjr.delete(car.get());
			return true;
		}else {
			return false;
		}
	}
//	
	@PutMapping("/update/{id}")
	public boolean updateCar(@PathVariable int id,@RequestBody Car c) {
		Optional<Car> optional = cjr.findById(id);
		if(optional.isPresent()) {
			Car car = optional.get();
			car.setBrand(c.getBrand());
			car.setPrice(c.getPrice());
			cjr.save(car);
			return true;
		}
		return false;
	}
//	
//	
	@PatchMapping("/update-price/{id}")
	public boolean updatePrice(@PathVariable int id,@RequestBody Car c) {
		Optional<Car> optional = cjr.findById(id);
		if(optional.isPresent()) {
			Car car = optional.get();
			car.setPrice(c.getPrice());
			cjr.save(car);
			return true;
		}
		else  return false;
	}
	
	@GetMapping("/price/{price}")
	public Car getCarByPrice(@PathVariable double price) {
		return cjr.getByPrice(price);
	}
	
	@ExceptionHandler(Exception.class)
	public String handleException() {
		return "Exception Handle Method";
	}
//	
	
	/*
	@PostMapping("/person")
	public List<String> getPerson(@RequestBody Person p){
		return p.getItem();
	}
//	
	@PostMapping("/collage")
	public String getStudentsWithCollage(@RequestBody College c) {
		return c.getName() + " " + c.getLocation() +  " \n" + c.getStudent();
	}
	
	*/
	
	/*
	@Autowired
	CustomerJpaRepository cjr1;
	
	@PostMapping("/customer")
	public void createCustomer(@RequestBody Customer c) {
		cjr1.save(c);
	}
//	
	@GetMapping("/customer/{id}")
	public Customer getCustomerById(@PathVariable long id) {
		Optional<Customer> c = cjr1.findById(id);
	
		return c.get();
		
	}
//	
	@GetMapping("/customers")
	public List<Customer> getAllCustomers(){
		return cjr1.findAll();
	}
//	
	@PutMapping("/update/{id}")
	public boolean updateCustomerById(@PathVariable Long id, @RequestBody Customer c) {
		Optional<Customer> optional = cjr1.findById(id);
		
		if(optional.isPresent()) {
			Customer customer = optional.get();
			customer.setPhone(c.getPhone());
			customer.setEmail(c.getEmail());
			customer.setName(c.getName());
			customer.setAge(c.getAge());
			customer.setGender(c.getGender());
			customer.setDob(c.getDob());

			
			cjr1.save(customer);
			return true;
			
		}
		return false;
//		
	}
//	
	@PatchMapping("/update-phone/{id}")
	public boolean updatePhoneById(@PathVariable Long id, @RequestBody Customer c) {
		Optional<Customer> optional = cjr1.findById(id);
		
		if(optional.isPresent()) {
			Customer customer = optional.get();
			customer.setPhone(c.getPhone());
			
			cjr1.save(customer);
			return true;
			
		}
		return false;
		
	}
//	
	@DeleteMapping("/delete/{id}")
	public boolean delete(@PathVariable long id) {
		Customer c = cjr1.findById(id).get();
		if(c != null) {
			cjr1.delete(c);
			return true;
		}
		return false;
	}
	
	 @GetMapping("/customer/email/{email}")
	 public Customer getByEmail(@PathVariable String email) {
	     return cjr1.findByEmail(email);
	 }
//
	 @GetMapping("/customer/name-email")
	 public Customer getByNameAndEmail(@RequestParam String name,
	                                   @RequestParam String email) {
	     return cjr1.findByNameAndEmail(name, email);
	 }
//
	 @GetMapping("/customer/name-or-email")
	 public Customer getByNameOrEmail(@RequestParam String name,
	                                  @RequestParam String email) {
	     return cjr1.findByNameOrEmail(name, email);
	 }
//
	 @GetMapping("/customer/id-between")
	 public List<Customer> getByIdBetween(@RequestParam Long start,
	                                      @RequestParam Long end) {
	     return cjr1.findByIdBetween(start, end);
	 }
//
	    @GetMapping("/customer/age-greater/{age}")
	    public List<Customer> getByAgeGreaterThan(@PathVariable Integer age) {
	        return cjr1.findByAgeGreaterThan(age);
	    }
//
	    @GetMapping("/customer/id-less/{id}")
	    public List<Customer> getByIdLessThan(@PathVariable Long id) {
	        return cjr1.findByIdLessThan(id);
	    }

	    @GetMapping("/customer/email-null")
	    public List<Customer> getEmailNull() {
	        return cjr1.findByEmailIsNull();
	    }
//
	    @GetMapping("/customer/email-not-null")
	    public List<Customer> getEmailNotNull() {
	        return cjr1.findByEmailIsNotNull();
	    }
//
	    @GetMapping("/customer/name-contains/{keyword}")
	    public List<Customer> getByNameContaining(@PathVariable String keyword) {
	        return cjr1.findByNameContaining(keyword);
	    }
//
	    @GetMapping("/customer/name-starts/{prefix}")
	    public List<Customer> getByNameStartingWith(@PathVariable String prefix) {
	        return cjr1.findByNameStartingWith(prefix);
	    }
//
	    @GetMapping("/customer/name-ends/{suffix}")
	    public List<Customer> getByNameEndingWith(@PathVariable String suffix) {
	        return cjr1.findByNameEndingWith(suffix);
	    }
//
	    @GetMapping("/customer/name-ignorecase/{name}")
	    public List<Customer> getByNameIgnoreCase(@PathVariable String name) {
	        return cjr1.findByNameIgnoreCase(name);
	    }
//
	    @GetMapping("/customer/latest")
	    public Customer getLatestCustomer() {
	        return cjr1.findTop1ByOrderByIdDesc();
	    }
//
	    @GetMapping("/customer/top3/{name}")
	    public List<Customer> getTop3ByName(@PathVariable String name) {
	        return cjr1.findFirst3ByName(name);
	    }
//	*/
	
}