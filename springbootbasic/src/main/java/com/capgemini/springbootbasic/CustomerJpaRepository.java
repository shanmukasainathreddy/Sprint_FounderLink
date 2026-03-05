package com.capgemini.springbootbasic;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerJpaRepository extends JpaRepository<Customer, Long> {

    Customer findByEmail(String email);

    Customer findByNameAndEmail(String name, String email);

    Customer findByNameOrEmail(String name, String email);

    List<Customer> findByIdBetween(Long start, Long end);

    List<Customer> findByAgeGreaterThan(Integer age);

    List<Customer> findByIdLessThan(Long id);

    List<Customer> findByEmailIsNull();

    List<Customer> findByEmailIsNotNull();

    List<Customer> findByNameContaining(String keyword);

    List<Customer> findByNameStartingWith(String prefix);

    List<Customer> findByNameEndingWith(String suffix);

    List<Customer> findByNameIgnoreCase(String name);

    Customer findTop1ByOrderByIdDesc();

    List<Customer> findFirst3ByName(String name);
}