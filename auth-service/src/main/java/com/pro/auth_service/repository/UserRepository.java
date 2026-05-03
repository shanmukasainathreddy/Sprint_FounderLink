package com.pro.auth_service.repository;



import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pro.auth_service.entity.User;



public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
