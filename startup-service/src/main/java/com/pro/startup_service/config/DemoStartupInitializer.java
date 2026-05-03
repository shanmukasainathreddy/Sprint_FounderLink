package com.pro.startup_service.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DemoStartupInitializer implements CommandLineRunner {

    @Override
    public void run(String... args) {
        log.info("Demo startup seeding disabled. Startups are created through the app.");
    }
}
