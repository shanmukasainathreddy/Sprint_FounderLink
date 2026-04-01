package com.pro.auth_service.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pro.auth_service.dto.AuthRequest;
import com.pro.auth_service.dto.EmailRequest;
import com.pro.auth_service.dto.VerifyOtpRequest;
import com.pro.auth_service.service.AuthService;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController controller;

    @Test
    void testRegister() {
        AuthRequest request = new AuthRequest();
        request.setEmail("user@test.com");
        request.setPassword("pass123");
        request.setRole("FOUNDER");

        when(authService.register("user@test.com", "pass123", "FOUNDER"))
                .thenReturn("Registration successful. Please verify the OTP sent to your email.");

        String response = controller.register(request);
        assertEquals("Registration successful. Please verify the OTP sent to your email.", response);
        verify(authService, times(1)).register("user@test.com", "pass123", "FOUNDER");
    }

    @Test
    void testVerifyOtp() {
        VerifyOtpRequest request = new VerifyOtpRequest();
        request.setEmail("user@test.com");
        request.setOtp("123456");

        when(authService.verifyOtp("user@test.com", "123456")).thenReturn("Email verified successfully");

        String response = controller.verifyOtp(request);
        assertEquals("Email verified successfully", response);
        verify(authService, times(1)).verifyOtp("user@test.com", "123456");
    }

    @Test
    void testResendOtp() {
        EmailRequest request = new EmailRequest();
        request.setEmail("user@test.com");

        when(authService.resendOtp("user@test.com")).thenReturn("OTP resent successfully");

        String response = controller.resendOtp(request);
        assertEquals("OTP resent successfully", response);
        verify(authService, times(1)).resendOtp("user@test.com");
    }

    @Test
    void testLogin() {
        AuthRequest request = new AuthRequest();
        request.setEmail("user@test.com");
        request.setPassword("pass123");
        request.setRole("FOUNDER");

        when(authService.login("user@test.com", "pass123", "FOUNDER")).thenReturn("mocked_token");

        String response = controller.login(request);
        assertEquals("mocked_token", response);
        verify(authService, times(1)).login("user@test.com", "pass123", "FOUNDER");
    }

    @Test
    void testTestEndpoint() {
        String response = controller.test();
        assertEquals("Auth working", response);
    }
}
