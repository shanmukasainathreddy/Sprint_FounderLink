package com.pro.auth_service.repository;



import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pro.auth_service.entity.Role;


public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}

