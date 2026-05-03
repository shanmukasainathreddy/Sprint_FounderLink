package com.pro.api_gateway.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

class HomeControllerTest {

    @Test
    void homeReturnsGatewayStatusAndRoutes() {
        HomeController controller = new HomeController();

        Map<String, Object> response = controller.home();

        assertEquals("api-gateway", response.get("service"));
        assertEquals("UP", response.get("status"));
        assertEquals("/actuator/health", response.get("health"));
        assertTrue(response.get("routes") instanceof Map);
    }
}
