package com.pro.user_service.repository;



import org.springframework.data.jpa.repository.JpaRepository;

import com.pro.user_service.entity.UserProfile;

public interface UserRepository extends JpaRepository<UserProfile, Long> {
    boolean existsByEmailIgnoreCase(String email);
}
