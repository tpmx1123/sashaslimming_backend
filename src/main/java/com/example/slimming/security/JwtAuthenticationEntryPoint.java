package com.example.slimming.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        // Add CORS headers to error response only if not already set by Spring Security CORS
        // This prevents duplicate headers
        String origin = request.getHeader("Origin");
        if (origin != null && (origin.contains("sashaslimming.com") || origin.contains("localhost"))) {
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
        // Send 401 for authentication failures
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Authentication required\"}");
    }
}
