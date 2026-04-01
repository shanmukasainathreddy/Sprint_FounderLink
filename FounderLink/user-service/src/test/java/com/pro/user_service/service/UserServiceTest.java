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

        when(userRepository.existsByEmailIgnoreCase("test@example.com")).thenReturn(false);
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
        when(userRepository.existsByEmailIgnoreCase("new@test.com")).thenReturn(false);
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
}
