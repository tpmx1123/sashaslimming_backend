package com.example.slimming.controller;

import com.example.slimming.entity.Admin;
import com.example.slimming.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = {"https://sashaslimming.com", "https://www.sashaslimming.com", "http://localhost:3000", "http://localhost:3001", "http://localhost:5173"})
public class AdminController {

    @Autowired
    private AdminRepository adminRepository;

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401)
                    .header("Content-Type", "application/json")
                    .body("{\"error\":\"Unauthorized\"}");
            }

            String username = authentication.getName();
            Admin admin = adminRepository.findByUsername(username)
                    .orElse(null);

            if (admin == null) {
                return ResponseEntity.status(404)
                    .header("Content-Type", "application/json")
                    .body("{\"error\":\"Admin not found\"}");
            }

            // Return admin profile without password
            Admin profileResponse = new Admin();
            profileResponse.setId(admin.getId());
            profileResponse.setUsername(admin.getUsername());
            profileResponse.setEmail(admin.getEmail());
            profileResponse.setRole(admin.getRole());
            profileResponse.setCreatedAt(admin.getCreatedAt());

            return ResponseEntity.ok(profileResponse);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .header("Content-Type", "application/json")
                .body("{\"error\":\"Failed to fetch profile: " + e.getMessage() + "\"}");
        }
    }
}

