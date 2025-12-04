package com.example.slimming.security;

import com.example.slimming.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestPath = request.getRequestURI();
        String method = request.getMethod();

        // Skip JWT processing for OPTIONS requests (CORS preflight)
        if ("OPTIONS".equalsIgnoreCase(method)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Skip JWT processing for public endpoints - let them pass through without authentication
        if (isPublicEndpoint(requestPath)) {
            // Log for debugging (remove in production if needed)
            System.out.println("JwtAuthenticationFilter: Allowing public endpoint: " + method + " " + requestPath);
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        // Only process JWT if token is present
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7);
                String username = jwtService.extractUsername(token);

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    if (jwtService.validateToken(token, userDetails)) {
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }
            } catch (Exception e) {
                // Log error but don't block the request - let Spring Security handle authorization
                System.err.println("JWT processing error: " + e.getMessage());
            }
        }

        // Always continue the filter chain - let Spring Security handle authorization
        filterChain.doFilter(request, response);
    }

    private boolean isPublicEndpoint(String path) {
        if (path == null) {
            return false;
        }
        
        // Normalize path (remove trailing slash for consistent matching)
        String normalizedPath = path.endsWith("/") && path.length() > 1 ? path.substring(0, path.length() - 1) : path;
        
        // Public endpoints that don't require authentication
        // Exclude change-password from public endpoints (it requires authentication)
        if (normalizedPath.startsWith("/api/auth/")) {
            // change-password requires authentication, so don't skip JWT processing
            if (normalizedPath.equals("/api/auth/change-password")) {
                return false;
            }
            return true;
        }
        
        // Other public endpoints - check exact matches and prefixes
        return normalizedPath.startsWith("/api/blogs/public/") ||
               normalizedPath.equals("/api/blogs/public") ||
               normalizedPath.startsWith("/api/newsletter/subscribe") ||
               normalizedPath.startsWith("/api/contact") ||
               // Only allow /api/bookings (POST for public bookings), NOT /api/bookings/admin/**
               (normalizedPath.equals("/api/bookings") || 
                (normalizedPath.startsWith("/api/bookings/") && !normalizedPath.startsWith("/api/bookings/admin/")));
    }
}
