package com.pro.auth_service.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StatusController {

    @GetMapping("/")
    public Map<String, String> home() {
        return Map.of(
                "service", "auth-service",
                "status", "UP",
                "health", "/actuator/health",
                "swagger", "/swagger-ui.html");
    }
}
