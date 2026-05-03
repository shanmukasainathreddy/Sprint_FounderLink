package com.pro.auth_service.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pro.auth_service.dto.AuthRequest;
import com.pro.auth_service.dto.EmailRequest;
import com.pro.auth_service.dto.ResetPasswordRequest;
import com.pro.auth_service.dto.VerifyOtpRequest;
import com.pro.auth_service.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public String register(@Valid @RequestBody AuthRequest request) {
        return authService.register(
                request.getEmail(),
                request.getPassword(),
                request.getRole(),
                request.getName(),
                request.getBio());
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        return authService.verifyOtp(request.getEmail(), request.getOtp());
    }

    @PostMapping("/resend-otp")
    public String resendOtp(@Valid @RequestBody EmailRequest request) {
        return authService.resendOtp(request.getEmail());
    }

    @PostMapping("/login")
    public String login(@Valid @RequestBody AuthRequest request) {
        return authService.login(request.getEmail(), request.getPassword(), request.getRole());
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(@Valid @RequestBody EmailRequest request) {
        return authService.requestPasswordReset(request.getEmail());
    }

    @PostMapping("/forgot-password/verify")
    public String verifyPasswordResetOtp(@Valid @RequestBody VerifyOtpRequest request) {
        return authService.verifyPasswordResetOtp(request.getEmail(), request.getOtp());
    }

    @PostMapping("/reset-password")
    public String resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return authService.resetPassword(
                request.getEmail(),
                request.getOtp(),
                request.getPassword(),
                request.getConfirmPassword());
    }

    @GetMapping("/user-roles")
    public Map<Long, String> getUserRoles() {
        return authService.getUserRoles();
    }

    @GetMapping("/test")
    public String test() {
        return "Auth working";
    }
}
