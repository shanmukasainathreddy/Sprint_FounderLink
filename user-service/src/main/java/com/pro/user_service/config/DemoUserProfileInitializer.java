package com.pro.user_service.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DemoUserProfileInitializer implements CommandLineRunner {

    @Override
    public void run(String... args) {
        log.info("Demo user profile seeding disabled. Profiles are created through registration.");
    }
}
