package com.example.slimming.controller;

import com.example.slimming.dto.AuthRequest;
import com.example.slimming.dto.AuthResponse;
import com.example.slimming.entity.Admin;
import com.example.slimming.repository.AdminRepository;
import com.example.slimming.service.EmailService;
import com.example.slimming.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"https://sashaslimming.com", "https://www.sashaslimming.com", "http://localhost:3000", "http://localhost:3001", "http://localhost:5173"})
public class AuthController {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private EmailService emailService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest) {
        try {
            // Validate request
            if (authRequest == null || 
                authRequest.getUsername() == null || authRequest.getUsername().trim().isEmpty() ||
                authRequest.getPassword() == null || authRequest.getPassword().trim().isEmpty()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Username and password are required");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            Admin admin = adminRepository.findByUsername(authRequest.getUsername().trim())
                    .orElse(null);
            if (admin == null || !passwordEncoder.matches(authRequest.getPassword(), admin.getPassword())) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Invalid username or password");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            String token = jwtService.generateToken(admin.getUsername());
            AuthResponse authResponse = new AuthResponse();
            authResponse.setToken(token);
            authResponse.setUsername(admin.getUsername());
            authResponse.setEmail(admin.getEmail());
            authResponse.setRole(admin.getRole());
            return ResponseEntity.ok(authResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Login failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthRequest authRequest) {
        try {
            // Validate request
            if (authRequest == null || 
                authRequest.getUsername() == null || authRequest.getUsername().trim().isEmpty() ||
                authRequest.getPassword() == null || authRequest.getPassword().trim().isEmpty()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Username and password are required");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            if (adminRepository.findByUsername(authRequest.getUsername().trim()).isPresent()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Username already exists");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            Admin admin = new Admin();
            admin.setUsername(authRequest.getUsername().trim());
            admin.setPassword(passwordEncoder.encode(authRequest.getPassword()));
            admin.setEmail(authRequest.getUsername().trim() + "@slimming.com");
            admin.setRole("ADMIN");

            adminRepository.save(admin);

            String token = jwtService.generateToken(admin.getUsername());

            AuthResponse authResponse = new AuthResponse();
            authResponse.setToken(token);
            authResponse.setUsername(admin.getUsername());
            authResponse.setEmail(admin.getEmail());
            authResponse.setRole(admin.getRole());

            return ResponseEntity.ok(authResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Registration failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            
            if (username == null || username.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .header("Content-Type", "application/json")
                    .body("{\"error\":\"Username is required\"}");
            }

            Admin admin = adminRepository.findByUsername(username.trim())
                    .orElse(null);

            if (admin == null) {
                // Don't reveal if username exists for security
                return ResponseEntity.ok()
                    .header("Content-Type", "application/json")
                    .body("{\"success\":true,\"message\":\"If the username exists, a password reset link has been sent to lumiereluxe0030@gmail.com\"}");
            }

            // Always send reset email to lumiereluxe0030@gmail.com (regardless of admin's stored email)
            System.out.println("Admin found: " + admin.getUsername() + ", stored email: " + admin.getEmail());
            System.out.println("Sending reset email to: lumiereluxe0030@gmail.com");
            
            // Generate reset token
            String resetToken = UUID.randomUUID().toString();
            admin.setResetToken(resetToken);
            admin.setResetTokenExpiry(LocalDateTime.now().plusHours(1)); // Token expires in 1 hour
            adminRepository.save(admin);

            // Send reset email - always to lumiereluxe0030@gmail.com
            try {
                emailService.sendPasswordResetEmail(resetToken, admin.getUsername());
                System.out.println("✅ Password reset email sent successfully for username: " + admin.getUsername());
            } catch (Exception emailException) {
                System.err.println("❌ Error sending password reset email: " + emailException.getMessage());
                emailException.printStackTrace();
                // Still return success to user (security best practice - don't reveal if email failed)
                // But log the error for admin debugging
                return ResponseEntity.ok()
                    .header("Content-Type", "application/json")
                    .body("{\"success\":true,\"message\":\"Password reset link has been sent to lumiereluxe0030@gmail.com\"}");
            }

            return ResponseEntity.ok()
                .header("Content-Type", "application/json")
                .body("{\"success\":true,\"message\":\"Password reset link has been sent to lumiereluxe0030@gmail.com\"}");
        } catch (Exception e) {
            System.err.println("Error in forgotPassword endpoint: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest()
                .header("Content-Type", "application/json")
                .body("{\"error\":\"Failed to process password reset request: " + e.getMessage() + "\"}");
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        try {
            String token = request.get("token");
            String newPassword = request.get("newPassword");

            if (token == null || token.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .header("Content-Type", "application/json")
                    .body("{\"error\":\"Reset token is required\"}");
            }

            if (newPassword == null || newPassword.trim().isEmpty() || newPassword.length() < 6) {
                return ResponseEntity.badRequest()
                    .header("Content-Type", "application/json")
                    .body("{\"error\":\"New password is required and must be at least 6 characters long\"}");
            }

            // Find admin by reset token
            Admin admin = adminRepository.findAll().stream()
                    .filter(a -> token.equals(a.getResetToken()) && 
                            a.getResetTokenExpiry() != null && 
                            a.getResetTokenExpiry().isAfter(LocalDateTime.now()))
                    .findFirst()
                    .orElse(null);

            if (admin == null) {
                return ResponseEntity.badRequest()
                    .header("Content-Type", "application/json")
                    .body("{\"error\":\"Invalid or expired reset token\"}");
            }

            // Update password and clear reset token
            admin.setPassword(passwordEncoder.encode(newPassword));
            admin.setResetToken(null);
            admin.setResetTokenExpiry(null);
            adminRepository.save(admin);

            return ResponseEntity.ok()
                .header("Content-Type", "application/json")
                .body("{\"success\":true,\"message\":\"Password reset successfully\"}");
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .header("Content-Type", "application/json")
                .body("{\"error\":\"Failed to reset password: " + e.getMessage() + "\"}");
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401)
                    .header("Content-Type", "application/json")
                    .body("{\"error\":\"Unauthorized\"}");
            }

            String username = authentication.getName();
            String currentPassword = request.get("currentPassword");
            String newPassword = request.get("newPassword");

            if (currentPassword == null || currentPassword.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .header("Content-Type", "application/json")
                    .body("{\"error\":\"Current password is required\"}");
            }

            if (newPassword == null || newPassword.trim().isEmpty() || newPassword.length() < 6) {
                return ResponseEntity.badRequest()
                    .header("Content-Type", "application/json")
                    .body("{\"error\":\"New password is required and must be at least 6 characters long\"}");
            }

            Admin admin = adminRepository.findByUsername(username)
                    .orElse(null);

            if (admin == null) {
                return ResponseEntity.status(404)
                    .header("Content-Type", "application/json")
                    .body("{\"error\":\"Admin not found\"}");
            }

            // Verify current password
            if (!passwordEncoder.matches(currentPassword, admin.getPassword())) {
                return ResponseEntity.badRequest()
                    .header("Content-Type", "application/json")
                    .body("{\"error\":\"Current password is incorrect\"}");
            }

            // Update password
            admin.setPassword(passwordEncoder.encode(newPassword));
            adminRepository.save(admin);

            return ResponseEntity.ok()
                .header("Content-Type", "application/json")
                .body("{\"success\":true,\"message\":\"Password changed successfully\"}");
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .header("Content-Type", "application/json")
                .body("{\"error\":\"Failed to change password: " + e.getMessage() + "\"}");
        }
    }
}
