package com.example.slimming.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        // Add CORS headers to 403 error response only if not already set by Spring Security CORS
        // This prevents duplicate headers
        String origin = request.getHeader("Origin");
        if (origin != null && (origin.contains("lumiereluxe.in") || origin.contains("localhost"))) {
            // Only set headers if they don't already exist (Spring Security CORS may have set them)
            if (response.getHeader("Access-Control-Allow-Origin") == null) {
                response.setHeader("Access-Control-Allow-Origin", origin);
            }
            if (response.getHeader("Access-Control-Allow-Credentials") == null) {
                response.setHeader("Access-Control-Allow-Credentials", "true");
            }
            if (response.getHeader("Access-Control-Allow-Methods") == null) {
                response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH");
            }
            if (response.getHeader("Access-Control-Allow-Headers") == null) {
                response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type, X-Requested-With");
            }
        }
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"Forbidden\",\"message\":\"Access denied\"}");
    }
}

