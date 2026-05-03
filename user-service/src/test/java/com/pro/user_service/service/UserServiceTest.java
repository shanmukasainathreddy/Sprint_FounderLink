package com.pro.user_service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pro.user_service.dto.UserRequest;
import com.pro.user_service.dto.UserProfileSyncRequest;
import com.pro.user_service.dto.UserSummaryResponse;
import com.pro.user_service.entity.UserProfile;
import com.pro.user_service.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void testCreate() {
        UserRequest user = new UserRequest();
        user.setName("Test User");
        user.setEmail("test@example.com");

        UserProfile savedUser = new UserProfile();
        savedUser.setId(1L);
        savedUser.setName("Test User");
        savedUser.setEmail("test@example.com");

        when(userRepository.findByEmailIgnoreCase("test@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(UserProfile.class))).thenReturn(savedUser);

        UserProfile result = userService.create(user);
        assertNotNull(result);
        assertEquals("Test User", result.getName());
        verify(userRepository, times(1)).save(any(UserProfile.class));
    }

    @Test
    void testGetAll() {
        UserProfile mockUser = new UserProfile();
        when(userRepository.findAll()).thenReturn(Arrays.asList(mockUser));

        List<UserProfile> result = userService.getAll();
        assertEquals(1, result.size());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void testGetByIdSuccess() {
        UserProfile user = new UserProfile();
        user.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserProfile result = userService.getById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void testGetByIdNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> userService.getById(1L));
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void testUpdateSuccess() {
        UserProfile existing = new UserProfile();
        existing.setId(1L);
        existing.setName("Old Name");
        existing.setEmail("old@test.com");

        UserRequest updatedInput = new UserRequest();
        updatedInput.setName("New Name");
        updatedInput.setEmail("new@test.com");
        updatedInput.setBio("New Bio");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.findByEmailIgnoreCase("new@test.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(UserProfile.class))).thenAnswer(invocation -> invocation.getArguments()[0]);

        UserProfile result = userService.update(1L, updatedInput);

        assertNotNull(result);
        assertEquals("New Name", result.getName());
        assertEquals("new@test.com", result.getEmail());
        assertEquals("New Bio", result.getBio());
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(existing);
    }

    @Test
    void testGetSummaryById() {
        UserProfile user = new UserProfile();
        user.setId(7L);
        user.setName("Summary User");
        user.setEmail("summary@test.com");

        when(userRepository.findById(7L)).thenReturn(Optional.of(user));

        UserSummaryResponse result = userService.getSummaryById(7L);

        assertEquals(7L, result.getId());
        assertEquals("summary@test.com", result.getEmail());
    }

    @Test
    void testSyncProfileReturnsSavedProfileWithoutUsingCachedLookup() {
        UserProfile savedProfile = new UserProfile();
        savedProfile.setId(4L);
        savedProfile.setName("Saved Founder");
        savedProfile.setEmail("founder@example.com");
        savedProfile.setBio("Saved profile bio");
        savedProfile.setSkills("fundraising");
        savedProfile.setExperience("10 years");
        savedProfile.setLocation("India");

        UserProfileSyncRequest request = new UserProfileSyncRequest();
        request.setId(4L);
        request.setName("Saved Founder");
        request.setEmail("founder@example.com");

        when(userRepository.findByEmailIgnoreCase("founder@example.com")).thenReturn(Optional.empty());
        when(userRepository.findById(4L)).thenReturn(Optional.of(savedProfile));

        UserProfile result = userService.syncProfile(request);

        assertEquals("Saved profile bio", result.getBio());
        assertEquals("fundraising", result.getSkills());
        assertEquals("10 years", result.getExperience());
        assertEquals("India", result.getLocation());
        verify(userRepository).upsertProfile(4L, "Saved Founder", "founder@example.com", "");
        verify(userRepository, times(1)).findById(4L);
    }

    @Test
    void testCreateRejectsDuplicateEmail() {
        UserRequest request = new UserRequest();
        request.setName("Duplicate");
        request.setEmail("duplicate@example.com");

        UserProfile existing = new UserProfile();
        existing.setId(1L);
        existing.setEmail("duplicate@example.com");

        when(userRepository.findByEmailIgnoreCase("duplicate@example.com")).thenReturn(Optional.of(existing));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> userService.create(request));

        assertEquals("Email already exists: duplicate@example.com", ex.getMessage());
    }

    @Test
    void testUpdateAllowsSameEmailForCurrentUser() {
        UserProfile existing = new UserProfile();
        existing.setId(1L);
        existing.setEmail("same@example.com");

        UserRequest request = new UserRequest();
        request.setName("Same User");
        request.setEmail("same@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.findByEmailIgnoreCase("same@example.com")).thenReturn(Optional.of(existing));
        when(userRepository.save(any(UserProfile.class))).thenAnswer(i -> i.getArguments()[0]);

        UserProfile result = userService.update(1L, request);

        assertEquals("Same User", result.getName());
        assertEquals("same@example.com", result.getEmail());
    }

    @Test
    void testGetDirectoryMapsProfilesToSummaries() {
        UserProfile profile = new UserProfile();
        profile.setId(1L);
        profile.setName("Directory User");
        profile.setEmail("directory@example.com");
        profile.setBio("Bio");
        profile.setSkills("Java");
        profile.setExperience("3 years");
        profile.setPortfolioLinks("https://example.com");
        profile.setLocation("India");

        when(userRepository.findAll()).thenReturn(List.of(profile));

        List<UserSummaryResponse> result = userService.getDirectory();

        assertEquals(1, result.size());
        assertEquals("Directory User", result.get(0).getName());
        assertEquals("Java", result.get(0).getSkills());
    }

    @Test
    void testSyncProfileResolvesNameFromEmailWhenNameBlank() {
        UserProfile savedProfile = new UserProfile();
        savedProfile.setId(8L);
        savedProfile.setName("founder user");
        savedProfile.setEmail("founder.user@example.com");

        UserProfileSyncRequest request = new UserProfileSyncRequest();
        request.setId(8L);
        request.setName(" ");
        request.setEmail("founder.user@example.com");
        request.setBio("Bio");

        when(userRepository.findByEmailIgnoreCase("founder.user@example.com")).thenReturn(Optional.empty());
        when(userRepository.findById(8L)).thenReturn(Optional.of(savedProfile));

        UserProfile result = userService.syncProfile(request);

        assertEquals("founder user", result.getName());
        verify(userRepository).upsertProfile(8L, "founder user", "founder.user@example.com", "Bio");
    }
}
