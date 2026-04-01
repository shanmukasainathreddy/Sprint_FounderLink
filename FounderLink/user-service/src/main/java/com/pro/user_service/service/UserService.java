package com.pro.user_service.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.pro.user_service.dto.UserRequest;
import com.pro.user_service.dto.UserSummaryResponse;
import com.pro.user_service.entity.UserProfile;
import com.pro.user_service.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @CachePut(value = "users", key = "#result.id")
    public UserProfile create(UserRequest request) {
        validateEmailUniqueness(request.getEmail(), null);

        UserProfile user = new UserProfile();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setBio(request.getBio());

        UserProfile savedUser = userRepository.save(user);
        log.info("Created user profile with id={}", savedUser.getId());
        return savedUser;
    }

    public List<UserProfile> getAll() {
        return userRepository.findAll();
    }

    @Cacheable(value = "users", key = "#id")
    public UserProfile getById(Long id) {
        log.debug("Fetching user profile with id={}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User profile not found for id " + id));
    }

    @CachePut(value = "users", key = "#id")
    public UserProfile update(Long id, UserRequest request) {
        UserProfile existing = getById(id);
        validateEmailUniqueness(request.getEmail(), id);
        existing.setName(request.getName());
        existing.setEmail(request.getEmail());
        existing.setBio(request.getBio());

        UserProfile updatedUser = userRepository.save(existing);
        log.info("Updated user profile with id={}", id);
        return updatedUser;
    }

    public UserSummaryResponse getSummaryById(Long id) {
        UserProfile profile = getById(id);
        return new UserSummaryResponse(profile.getId(), profile.getName(), profile.getEmail(), profile.getBio());
    }

    private void validateEmailUniqueness(String email, Long currentUserId) {
        boolean emailExists = userRepository.existsByEmailIgnoreCase(email);
        if (!emailExists) {
            return;
        }

        if (currentUserId != null) {
            UserProfile currentUser = getById(currentUserId);
            if (currentUser.getEmail().equalsIgnoreCase(email)) {
                return;
            }
        }

        throw new IllegalArgumentException("Email already exists: " + email);
    }
}
