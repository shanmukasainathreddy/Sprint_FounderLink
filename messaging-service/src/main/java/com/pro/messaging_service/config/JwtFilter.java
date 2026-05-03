package com.pro.messaging_service.config;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                   HttpServletResponse response,
                                   FilterChain filterChain)
            throws ServletException, IOException {


        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {

            String token = header.substring(7);
            System.out.println("✅ TOKEN: " + token);

            try {
                Claims claims = jwtUtil.extractClaims(token);

                String username = claims.getSubject();

                // ✅ Extract roles safely
                Object rolesObj = claims.get("roles");

                List<String> roles;

                if (rolesObj instanceof List<?>) {
                    roles = ((List<?>) rolesObj).stream()
                            .map(Object::toString)
                            .map(String::trim)
                            .toList();
                } else if (rolesObj instanceof String) {
                    roles = List.of((String) rolesObj);
                } else {
                    roles = List.of();
                }

                System.out.println("✅ ROLES: " + roles);

                // ✅ Convert roles → authorities
                List<SimpleGrantedAuthority> authorities =
                        roles.stream()
                             .map(this::normalizeRole)
                             .map(SimpleGrantedAuthority::new)
                             .toList();

                // ✅ Set authentication if not already set
                if (SecurityContextHolder.getContext().getAuthentication() == null) {

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    username,
                                    null,
                                    authorities
                            );

                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    System.out.println("✅ AUTH SET: " + authentication);
                }

            } catch (Exception e) {
                System.out.println("❌ JWT ERROR: " + e.getMessage());

                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid or Expired Token");
                return;
            }
        } else {
            System.out.println("⚠️ No Authorization Header (allowed for public APIs)");
        }

        filterChain.doFilter(request, response);
    }

    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return role;
        }

        String normalizedRole = role.trim().toUpperCase(Locale.ROOT);
        return normalizedRole.startsWith("ROLE_") ? normalizedRole : "ROLE_" + normalizedRole;
    }
}
