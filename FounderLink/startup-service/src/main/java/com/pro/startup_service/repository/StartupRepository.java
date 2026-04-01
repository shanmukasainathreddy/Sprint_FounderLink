package com.pro.startup_service.repository;



import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pro.startup_service.entity.Startup;

public interface StartupRepository extends JpaRepository<Startup, Long> {

    List<Startup> findByTitleContainingIgnoreCase(String keyword);
}
