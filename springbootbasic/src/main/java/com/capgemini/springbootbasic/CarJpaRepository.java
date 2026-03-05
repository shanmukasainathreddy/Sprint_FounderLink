package com.capgemini.springbootbasic;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;

public interface CarJpaRepository extends JpaRepository<Car, Integer>{
	public Car getByPrice(double price);
	
	@Modifying
	
	public int deleteByBrand(@Param("carbrand") String brand);
}