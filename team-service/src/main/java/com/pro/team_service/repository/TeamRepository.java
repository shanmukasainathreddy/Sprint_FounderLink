package com.pro.team_service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pro.team_service.entity.Team;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    List<Team> findByStartupId(Long startupId);
    
    List<Team> findByUserId(Long userId);

    List<Team> findByStartupIdAndStatus(Long startupId, String status);

    List<Team> findByUserIdAndStatus(Long userId, String status);

    Optional<Team> findByStartupIdAndUserId(Long startupId, Long userId);
}
