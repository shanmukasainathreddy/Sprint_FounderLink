package com.pro.user_service.repository;



import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pro.user_service.entity.UserProfile;

public interface UserRepository extends JpaRepository<UserProfile, Long> {
    boolean existsByEmailIgnoreCase(String email);
    Optional<UserProfile> findByEmailIgnoreCase(String email);

    @Modifying
    @Query(value = """
            INSERT INTO user_profiles (id, name, email, bio)
            VALUES (:id, :name, :email, :bio)
            ON CONFLICT (id) DO UPDATE
            SET name = COALESCE(NULLIF(EXCLUDED.name, ''), user_profiles.name),
                email = EXCLUDED.email,
                bio = COALESCE(NULLIF(EXCLUDED.bio, ''), user_profiles.bio)
            """, nativeQuery = true)
    void upsertProfile(
            @Param("id") Long id,
            @Param("name") String name,
            @Param("email") String email,
            @Param("bio") String bio);
}
