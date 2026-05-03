package com.pro.user_service.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.impl.DefaultClaims;

class JwtFilterTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldNormalizeRolesWithoutPrefix() throws Exception {
        JwtUtil jwtUtil = org.mockito.Mockito.mock(JwtUtil.class);
        JwtFilter jwtFilter = new JwtFilter(jwtUtil);

        Claims claims = new DefaultClaims();
        claims.setSubject("123");
        claims.put("roles", List.of("FOUNDER", "admin"));

        when(jwtUtil.extractClaims("token")).thenReturn(claims);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token");

        jwtFilter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals("123", authentication.getName());
        assertEquals(
                List.of("ROLE_FOUNDER", "ROLE_ADMIN"),
                authentication.getAuthorities().stream().map(Object::toString).toList());
    }
}
