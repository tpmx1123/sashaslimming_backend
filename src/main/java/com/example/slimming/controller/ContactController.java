package com.example.slimming.controller;

import com.example.slimming.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/contact")
@CrossOrigin(origins = {"https://sashaslimming.com", "https://www.sashaslimming.com", "http://localhost:3000", "http://localhost:3001", "http://localhost:5173"})
public class ContactController {

    @Autowired
    private EmailService emailService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> submitContactForm(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String name = request.get("name");
            String email = request.get("email");
            String phone = request.get("phone");
            String subject = request.get("subject");
            String message = request.get("message");

            // Validate required fields
            if (email == null || email.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Email is required");
                return ResponseEntity.badRequest().body(response);
            }

            if (name == null || name.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Name is required");
                return ResponseEntity.badRequest().body(response);
            }

            if (message == null || message.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Message is required");
                return ResponseEntity.badRequest().body(response);
            }

            // Send email
            emailService.sendContactFormEmail(name, email, phone, subject, message);

            response.put("success", true);
            response.put("message", "Your message has been sent successfully! We'll get back to you soon.");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Failed to send contact form email: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Failed to send message. Please try again later.");
            return ResponseEntity.status(500).body(response);
        }
    }
}

