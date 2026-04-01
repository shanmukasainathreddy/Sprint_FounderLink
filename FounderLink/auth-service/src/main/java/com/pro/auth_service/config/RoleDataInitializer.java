package com.pro.auth_service.config;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.pro.auth_service.entity.Role;
import com.pro.auth_service.repository.RoleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class RoleDataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        List<String> defaultRoles = List.of("FOUNDER", "INVESTOR", "COFOUNDER", "ADMIN");
        for (String roleName : defaultRoles) {
            ensureRole(roleName);
        }
    }

    private void ensureRole(String roleName) {
        boolean exists = roleRepository.existsByNameIgnoreCase(roleName)
                || roleRepository.existsByNameIgnoreCase("ROLE_" + roleName);
        if (!exists) {
            Role role = new Role();
            role.setName(roleName);
            roleRepository.save(role);
            log.info("Seeded missing role={}", roleName);
        }
    }
}
