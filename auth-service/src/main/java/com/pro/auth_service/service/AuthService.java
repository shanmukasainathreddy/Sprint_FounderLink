package com.pro.auth_service.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Locale;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.pro.auth_service.client.UserProfileClient;
import com.pro.auth_service.dto.NotificationEvent;
import com.pro.auth_service.dto.UserProfileSyncRequest;
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
    private final UserProfileClient userProfileClient;

    public String register(String email, String password, String roleName, String name, String bio) {
        User user = userRepository.findByEmail(email)
                .map(existingUser -> preparePendingUser(existingUser, password, roleName, name, bio))
                .orElseGet(() -> createPendingUser(email, password, roleName, name, bio));

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
        } else if (roleName != null && !roleName.isBlank()) {
            requireMatchingLoginRole(roleName, roles);
        }
        ensureUserProfile(user, null, null);
        log.info("User login successful for id={} with roles={}", user.getId(), roles);
        return jwtUtil.generateToken(String.valueOf(user.getId()), roles);
    }

    public String requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        user.setOtpCode(generateOtp());
        user.setOtpExpiryAt(LocalDateTime.now().plusMinutes(10));
        userRepository.save(user);
        notificationProducer.sendNotification(new NotificationEvent(
                user.getEmail(),
                "Your FounderLink password reset OTP is " + user.getOtpCode() + ". It expires in 10 minutes."));
        log.info("Password reset OTP sent for user id={}", user.getId());
        return "Password reset OTP sent to your email.";
    }

    public String verifyPasswordResetOtp(String email, String otp) {
        validatePasswordResetOtp(email, otp);
        return "OTP verified. Create your new password.";
    }

    public String resetPassword(String email, String otp, String password, String confirmPassword) {
        User user = validatePasswordResetOtp(email, otp);
        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        user.setPassword(passwordEncoder.encode(password));
        user.setOtpCode(null);
        user.setOtpExpiryAt(null);
        user.setEnabled(true);
        userRepository.save(user);
        notificationProducer.sendNotification(new NotificationEvent(
                user.getEmail(),
                "Your FounderLink password was reset successfully. You can now sign in with your new password."));
        log.info("Password reset completed for user id={}", user.getId());
        return "Password reset successfully. Please sign in with your new password.";
    }

    public Map<Long, String> getUserRoles() {
        Map<Long, String> rolesByUserId = new HashMap<>();
        for (User user : userRepository.findAll()) {
            List<String> roles = userRoleRepository.findRolesByUserId(user.getId());
            if (!roles.isEmpty()) {
                rolesByUserId.put(user.getId(), roles.get(0));
            }
        }
        return rolesByUserId;
    }

    private User createPendingUser(String email, String password, String roleName, String name, String bio) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        assignOtp(user);

        User savedUser = userRepository.save(user);
        Role role = resolveRole(roleName);
        ensureUserRole(savedUser.getId(), role.getId());
        ensureUserProfile(savedUser, name, bio);
        return savedUser;
    }

    private User preparePendingUser(User user, String password, String roleName, String name, String bio) {
        if (user.isEnabled()) {
            throw new IllegalArgumentException("User already exists with email " + user.getEmail());
        }

        user.setPassword(passwordEncoder.encode(password));
        assignOtp(user);
        User savedUser = userRepository.save(user);
        Role role = resolveRole(roleName);
        ensureUserRole(savedUser.getId(), role.getId());
        ensureUserProfile(savedUser, name, bio);
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
        if (normalizedRole.startsWith("ROLE_")) {
            normalizedRole = normalizedRole.substring("ROLE_".length());
        }
        normalizedRole = normalizedRole
                .replace("-", "")
                .replace("_", "")
                .replace(" ", "");
        return "COFUNDER".equals(normalizedRole) ? "COFOUNDER" : normalizedRole;
    }

    private void requireMatchingLoginRole(String requestedRole, List<String> assignedRoles) {
        String normalizedRequestedRole = normalizeRoleName(requestedRole);
        boolean matchesAssignedRole = assignedRoles.stream()
                .map(this::normalizeRoleName)
                .anyMatch(normalizedRequestedRole::equals);

        if (!matchesAssignedRole) {
            throw new IllegalArgumentException("Selected role does not match this account");
        }
    }

    private void sendOtpNotification(String email, String otp) {
        notificationProducer.sendNotification(new NotificationEvent(
                email,
                "Your FounderLink OTP is " + otp + ". It expires in 10 minutes."));
    }

    private User validatePasswordResetOtp(String email, String otp) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        if (user.getOtpCode() == null || user.getOtpExpiryAt() == null) {
            throw new IllegalArgumentException("No password reset OTP found. Please request a new OTP.");
        }
        if (user.getOtpExpiryAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("OTP expired. Please request a new OTP.");
        }
        if (!user.getOtpCode().equals(otp)) {
            throw new IllegalArgumentException("Invalid OTP");
        }
        return user;
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

    private void ensureUserProfile(User user, String name, String bio) {
        UserProfileSyncRequest request = new UserProfileSyncRequest();
        request.setId(user.getId());
        request.setEmail(user.getEmail());
        request.setName(resolveDisplayName(user.getEmail(), name));
        request.setBio(bio == null ? "" : bio.trim());

        try {
            userProfileClient.syncProfile(request);
        } catch (RuntimeException ex) {
            log.error("Failed to sync profile for auth user id={}", user.getId(), ex);
            throw new IllegalStateException("Unable to sync user profile. Please try again.");
        }
    }

    private String resolveDisplayName(String email, String name) {
        if (name != null && !name.isBlank()) {
            return name.trim();
        }

        String localPart = email == null ? "FounderLink User" : email.split("@")[0];
        String normalized = localPart.replace('.', ' ').replace('_', ' ').trim();
        return normalized.isBlank() ? "FounderLink User" : normalized;
    }
}
