package com.pro.api_gateway.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/gateway/status")
    public Map<String, Object> home() {
        return Map.of(
                "service", "api-gateway",
                "status", "UP",
                "swagger", "/swagger-ui.html",
                "health", "/actuator/health",
                "routes", Map.of(
                        "auth", "/auth",
                        "users", "/users",
                        "startups", "/startups",
                        "investments", "/investments",
                        "teams", "/teams",
                        "messages", "/messages",
                        "notifications", "/api/notifications"));
    }
}
