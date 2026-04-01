package com.pro.auth_service.service;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.pro.auth_service.dto.NotificationEvent;
import com.pro.auth_service.entity.Role;
import com.pro.auth_service.entity.User;
import com.pro.auth_service.entity.UserRole;
import com.pro.auth_service.producer.NotificationProducer;
import com.pro.auth_service.repository.RoleRepository;
import com.pro.auth_service.repository.UserRepository;
import com.pro.auth_service.repository.UserRoleRepository;
import com.pro.auth_service.util.JwtUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final NotificationProducer notificationProducer;

    public String register(String email, String password, String roleName) {
        User user = userRepository.findByEmail(email)
                .map(existingUser -> preparePendingUser(existingUser, password, roleName))
                .orElseGet(() -> createPendingUser(email, password, roleName));

        sendOtpNotification(user.getEmail(), user.getOtpCode());
        log.info("Registration OTP sent for user id={}", user.getId());
        return "Registration successful. Please verify the OTP sent to your email.";
    }

    public String verifyOtp(String email, String otp) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        if (user.isEnabled()) {
            throw new IllegalArgumentException("Email is already verified");
        }
        if (user.getOtpCode() == null || user.getOtpExpiryAt() == null) {
            throw new IllegalArgumentException("No OTP found. Please register or resend OTP.");
        }
        if (user.getOtpExpiryAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("OTP expired. Please resend OTP.");
        }
        if (!user.getOtpCode().equals(otp)) {
            throw new IllegalArgumentException("Invalid OTP");
        }

        user.setEnabled(true);
        user.setOtpCode(null);
        user.setOtpExpiryAt(null);
        userRepository.save(user);
        notificationProducer.sendNotification(new NotificationEvent(
                user.getEmail(),
                "Your FounderLink email has been verified successfully. Welcome aboard."));
        log.info("Email verified for user id={}", user.getId());
        return "Email verified successfully";
    }

    public String resendOtp(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        if (user.isEnabled()) {
            throw new IllegalArgumentException("Email is already verified");
        }

        assignOtp(user);
        userRepository.save(user);
        sendOtpNotification(user.getEmail(), user.getOtpCode());
        log.info("OTP resent for user id={}", user.getId());
        return "OTP resent successfully";
    }

    public String login(String email, String password, String roleName) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        if (!user.isEnabled()) {
            assignOtp(user);
            userRepository.save(user);
            sendOtpNotification(user.getEmail(), user.getOtpCode());
            throw new IllegalArgumentException("Email is not verified. A new OTP has been sent to your email.");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Invalid password");
        }

        List<String> roles = userRoleRepository.findRolesByUserId(user.getId());
        if (roles.isEmpty()) {
            Role role = resolveRole(roleName);
            ensureUserRole(user.getId(), role.getId());
            roles = List.of(role.getName());
            log.warn("User id={} had no roles. Assigned role={} during login recovery.", user.getId(), role.getName());
        }
        log.info("User login successful for id={} with roles={}", user.getId(), roles);
        return jwtUtil.generateToken(String.valueOf(user.getId()), roles);
    }

    private User createPendingUser(String email, String password, String roleName) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        assignOtp(user);

        User savedUser = userRepository.save(user);
        Role role = resolveRole(roleName);
        ensureUserRole(savedUser.getId(), role.getId());
        return savedUser;
    }

    private User preparePendingUser(User user, String password, String roleName) {
        if (user.isEnabled()) {
            throw new IllegalArgumentException("User already exists with email " + user.getEmail());
        }

        user.setPassword(passwordEncoder.encode(password));
        assignOtp(user);
        User savedUser = userRepository.save(user);
        Role role = resolveRole(roleName);
        ensureUserRole(savedUser.getId(), role.getId());
        return savedUser;
    }

    private void assignOtp(User user) {
        user.setOtpCode(generateOtp());
        user.setOtpExpiryAt(LocalDateTime.now().plusMinutes(10));
        user.setEnabled(false);
    }

    private Role resolveRole(String roleName) {
        String normalizedRole = normalizeRoleName(roleName);
        return roleRepository.findByNameIgnoreCase(normalizedRole)
                .or(() -> roleRepository.findByNameIgnoreCase("ROLE_" + normalizedRole))
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + normalizedRole));
    }

    private String normalizeRoleName(String roleName) {
        if (roleName == null || roleName.isBlank()) {
            throw new IllegalArgumentException("Role is required");
        }

        String normalizedRole = roleName.trim().toUpperCase(Locale.ROOT);
        return normalizedRole.startsWith("ROLE_")
                ? normalizedRole.substring("ROLE_".length())
                : normalizedRole;
    }

    private void sendOtpNotification(String email, String otp) {
        notificationProducer.sendNotification(new NotificationEvent(
                email,
                "Your FounderLink OTP is " + otp + ". It expires in 10 minutes."));
    }

    private void ensureUserRole(Long userId, Long roleId) {
        boolean alreadyAssigned = userRoleRepository.existsByUserIdAndRoleId(userId, roleId);
        if (!alreadyAssigned) {
            UserRole userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            userRoleRepository.save(userRole);
        }
    }

    private String generateOtp() {
        int otp = ThreadLocalRandom.current().nextInt(100000, 1000000);
        return String.valueOf(otp);
    }
}
