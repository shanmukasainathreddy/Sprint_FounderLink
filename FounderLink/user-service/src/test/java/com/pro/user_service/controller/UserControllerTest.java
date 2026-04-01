package com.pro.user_service.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pro.user_service.dto.UserRequest;
import com.pro.user_service.dto.UserSummaryResponse;
import com.pro.user_service.entity.UserProfile;
import com.pro.user_service.service.UserService;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Test
    void testCreate() {
        UserRequest requestUser = new UserRequest();
        requestUser.setName("New User");
        requestUser.setEmail("new@user.com");

        UserProfile savedUser = new UserProfile();
        savedUser.setName("New User");

        when(userService.create(any(UserRequest.class))).thenReturn(savedUser);

        UserProfile result = userController.create(requestUser);
        assertNotNull(result);
        assertEquals("New User", result.getName());
        verify(userService, times(1)).create(requestUser);
    }

    @Test
    void testGetAll() {
        UserProfile user = new UserProfile();
        when(userService.getAll()).thenReturn(Arrays.asList(user));

        List<UserProfile> result = userController.getAll();
        assertEquals(1, result.size());
        verify(userService, times(1)).getAll();
    }

    @Test
    void testGetById() {
        UserProfile user = new UserProfile();
        user.setId(1L);
        when(userService.getById(1L)).thenReturn(user);

        UserProfile result = userController.getById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(userService, times(1)).getById(1L);
    }

    @Test
    void testGetInternalById() {
        UserSummaryResponse user = new UserSummaryResponse(1L, "User", "user@test.com", "bio");
        when(userService.getSummaryById(1L)).thenReturn(user);

        UserSummaryResponse result = userController.getInternalById(1L);

        assertEquals("user@test.com", result.getEmail());
        verify(userService, times(1)).getSummaryById(1L);
    }

    @Test
    void testUpdate() {
        UserRequest request = new UserRequest();
        request.setName("Updated");
        request.setEmail("updated@test.com");

        UserProfile user = new UserProfile();
        user.setId(1L);
        user.setName("Updated");

        when(userService.update(eq(1L), any(UserRequest.class))).thenReturn(user);

        UserProfile result = userController.update(1L, request);
        assertNotNull(result);
        assertEquals("Updated", result.getName());
        verify(userService, times(1)).update(1L, request);
    }
}
