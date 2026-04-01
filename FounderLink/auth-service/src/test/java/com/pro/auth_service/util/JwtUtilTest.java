package com.pro.auth_service.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
    }

    // ─── generateToken ─────────────────────────────────────────────────────────

    @Test
    void testGenerateToken_returnsNonNullToken() {
        List<String> roles = Arrays.asList("ROLE_FOUNDER");
        String token = jwtUtil.generateToken("42", roles);
        assertNotNull(token);
    }

    @Test
    void testGenerateToken_containsThreeParts() {
        // JWT format: header.payload.signature
        String token = jwtUtil.generateToken("1", Arrays.asList("ROLE_INVESTOR"));
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length, "JWT must have 3 parts (header.payload.signature)");
    }

    // ─── extractEmail (subject) ────────────────────────────────────────────────

    @Test
    void testExtractEmail_returnsCorrectSubject() {
        String token = jwtUtil.generateToken("99", Arrays.asList("ROLE_FOUNDER"));
        String subject = jwtUtil.extractEmail(token);
        assertEquals("99", subject);
    }

    // ─── extractRoles ──────────────────────────────────────────────────────────

    @Test
    void testExtractRoles_returnsCorrectRoles() {
        List<String> roles = Arrays.asList("ROLE_FOUNDER", "ROLE_ADMIN");
        String token = jwtUtil.generateToken("10", roles);

        List<String> extracted = jwtUtil.extractRoles(token);
        assertNotNull(extracted);
        assertEquals(2, extracted.size());
        assertEquals("ROLE_FOUNDER", extracted.get(0));
        assertEquals("ROLE_ADMIN", extracted.get(1));
    }

    @Test
    void testExtractRoles_singleRole() {
        List<String> roles = Arrays.asList("ROLE_INVESTOR");
        String token = jwtUtil.generateToken("5", roles);

        List<String> extracted = jwtUtil.extractRoles(token);
        assertEquals(1, extracted.size());
        assertEquals("ROLE_INVESTOR", extracted.get(0));
    }

    // ─── validateToken ─────────────────────────────────────────────────────────

    @Test
    void testValidateToken_doesNotThrowForValidToken() {
        String token = jwtUtil.generateToken("7", Arrays.asList("ROLE_FOUNDER"));
        assertDoesNotThrow(() -> jwtUtil.validateToken(token));
    }

    @Test
    void testValidateToken_throwsForTamperedToken() {
        String token = jwtUtil.generateToken("7", Arrays.asList("ROLE_FOUNDER"));
        String tampered = token + "tampered";
        assertThrows(Exception.class, () -> jwtUtil.validateToken(tampered));
    }

    @Test
    void testValidateToken_throwsForInvalidToken() {
        assertThrows(Exception.class, () -> jwtUtil.validateToken("not.a.valid.jwt"));
    }
}
