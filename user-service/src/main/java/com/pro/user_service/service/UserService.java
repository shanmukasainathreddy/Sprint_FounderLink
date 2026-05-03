package com.pro.user_service.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pro.user_service.dto.UserRequest;
import com.pro.user_service.dto.UserProfileSyncRequest;
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
        applyProfileFields(user, request);

        UserProfile savedUser = userRepository.save(user);
        log.info("Created user profile with id={}", savedUser.getId());
        return savedUser;
    }

    public List<UserProfile> getAll() {
        return userRepository.findAll();
    }

    public List<UserSummaryResponse> getDirectory() {
        return userRepository.findAll().stream()
                .map(this::toSummaryResponse)
                .toList();
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
        applyProfileFields(existing, request);

        UserProfile updatedUser = userRepository.save(existing);
        log.info("Updated user profile with id={}", id);
        return updatedUser;
    }

    public UserSummaryResponse getSummaryById(Long id) {
        UserProfile profile = getById(id);
        return toSummaryResponse(profile);
    }

    @Transactional
    @CachePut(value = "users", key = "#request.id")
    public UserProfile syncProfile(UserProfileSyncRequest request) {
        String name = resolveName(request.getName(), request.getEmail());
        String bio = request.getBio() == null ? "" : request.getBio();
        validateEmailUniqueness(request.getEmail(), request.getId());

        userRepository.upsertProfile(request.getId(), name, request.getEmail(), bio);
        UserProfile savedProfile = userRepository.findById(request.getId())
                .orElseThrow(() -> new NoSuchElementException("User profile not found for id " + request.getId()));
        log.info("Synced user profile with id={}", savedProfile.getId());
        return savedProfile;
    }

    private void validateEmailUniqueness(String email, Long currentUserId) {
        UserProfile existingProfile = userRepository.findByEmailIgnoreCase(email).orElse(null);
        if (existingProfile == null) {
            return;
        }

        if (currentUserId != null && currentUserId.equals(existingProfile.getId())) {
            return;
        }

        throw new IllegalArgumentException("Email already exists: " + email);
    }

    private String resolveName(String name, String email) {
        if (name != null && !name.isBlank()) {
            return name.trim();
        }
        if (email == null || email.isBlank()) {
            return "FounderLink User";
        }

        String localPart = email.split("@")[0].replace('.', ' ').replace('_', ' ').trim();
        return localPart.isBlank() ? "FounderLink User" : localPart;
    }

    private void applyProfileFields(UserProfile profile, UserRequest request) {
        profile.setSkills(request.getSkills());
        profile.setExperience(request.getExperience());
        profile.setLocation(request.getLocation());
        profile.setPortfolioLinks(request.getPortfolioLinks());
        profile.setBio(request.getBio());
    }

    private UserSummaryResponse toSummaryResponse(UserProfile profile) {
        return new UserSummaryResponse(
                profile.getId(),
                profile.getName(),
                profile.getEmail(),
                profile.getBio(),
                profile.getSkills(),
                profile.getExperience(),
                profile.getPortfolioLinks(),
                profile.getLocation());
    }
}
