package com.pro.auth_service.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.pro.auth_service.entity.Role;
import com.pro.auth_service.entity.User;
import com.pro.auth_service.entity.UserRole;
import com.pro.auth_service.repository.RoleRepository;
import com.pro.auth_service.repository.UserRepository;
import com.pro.auth_service.repository.UserRoleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class DemoAuthDataInitializer implements CommandLineRunner {

    private static final String ADMIN_EMAIL = "admin@founderlink.com";
    private static final String ADMIN_PASSWORD = "admin123";
    private static final String ADMIN_ROLE = "ADMIN";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        Role adminRole = roleRepository.findByNameIgnoreCase(ADMIN_ROLE)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName(ADMIN_ROLE);
                    Role savedRole = roleRepository.save(role);
                    log.info("Seeded missing role={}", ADMIN_ROLE);
                    return savedRole;
                });

        User admin = userRepository.findByEmail(ADMIN_EMAIL)
                .orElseGet(User::new);
        admin.setEmail(ADMIN_EMAIL);
        admin.setPassword(passwordEncoder.encode(ADMIN_PASSWORD));
        admin.setEnabled(true);
        admin.setOtpCode(null);
        admin.setOtpExpiryAt(null);

        User savedAdmin = userRepository.save(admin);
        if (!userRoleRepository.existsByUserIdAndRoleId(savedAdmin.getId(), adminRole.getId())) {
            UserRole userRole = new UserRole();
            userRole.setUserId(savedAdmin.getId());
            userRole.setRoleId(adminRole.getId());
            userRoleRepository.save(userRole);
        }

        log.info("Seeded admin login: email={}, password={}", ADMIN_EMAIL, ADMIN_PASSWORD);
    }
}
