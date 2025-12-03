package com.example.slimming.controller;

import com.example.slimming.entity.NewsletterSubscriber;
import com.example.slimming.repository.NewsletterSubscriberRepository;
import com.example.slimming.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/newsletter")
@CrossOrigin(origins = {"https://lumiereluxe.in", "https://www.lumiereluxe.in", "http://localhost:3000", "http://localhost:3001", "http://localhost:5173"})
public class NewsletterController {

    @Autowired
    private NewsletterSubscriberRepository subscriberRepository;

    @Autowired
    private EmailService emailService;

    // Public endpoint - Subscribe to newsletter
    @PostMapping("/subscribe")
    public ResponseEntity<Map<String, Object>> subscribe(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        Map<String, Object> response = new HashMap<>();

        if (email == null || email.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "Email is required");
            return ResponseEntity.badRequest().body(response);
        }

        email = email.trim().toLowerCase();

        // Check if email already exists
        Optional<NewsletterSubscriber> existingSubscriber = subscriberRepository.findByEmail(email);
        
        if (existingSubscriber.isPresent()) {
            NewsletterSubscriber subscriber = existingSubscriber.get();
            if (subscriber.getIsActive()) {
                response.put("success", false);
                response.put("message", "This email is already subscribed");
                return ResponseEntity.badRequest().body(response);
            } else {
                // Reactivate existing subscriber
                subscriber.setIsActive(true);
                subscriberRepository.save(subscriber);
                emailService.sendWelcomeEmail(email);
                response.put("success", true);
                response.put("message", "Successfully resubscribed to newsletter");
                return ResponseEntity.ok(response);
            }
        }

        // Create new subscriber
        NewsletterSubscriber newSubscriber = new NewsletterSubscriber();
        newSubscriber.setEmail(email);
        newSubscriber.setIsActive(true);
        subscriberRepository.save(newSubscriber);
        
        emailService.sendWelcomeEmail(email);
        
        response.put("success", true);
        response.put("message", "Successfully subscribed to newsletter");
        return ResponseEntity.ok(response);
    }

    // Admin endpoints - Require JWT authentication
    @GetMapping("/admin/all")
    public ResponseEntity<List<NewsletterSubscriber>> getAllSubscribers() {
        List<NewsletterSubscriber> subscribers = subscriberRepository.findAllByOrderBySubscribedAtDesc();
        return ResponseEntity.ok(subscribers);
    }

    @GetMapping("/admin/active")
    public ResponseEntity<List<NewsletterSubscriber>> getActiveSubscribers() {
        List<NewsletterSubscriber> activeSubscribers = subscriberRepository.findByIsActiveTrue();
        return ResponseEntity.ok(activeSubscribers);
    }

    @PostMapping("/admin/add")
    public ResponseEntity<Map<String, Object>> addSubscriber(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        Map<String, Object> response = new HashMap<>();

        if (email == null || email.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "Email is required");
            return ResponseEntity.badRequest().body(response);
        }

        email = email.trim().toLowerCase();

        Optional<NewsletterSubscriber> existingSubscriber = subscriberRepository.findByEmail(email);
        
        if (existingSubscriber.isPresent()) {
            NewsletterSubscriber subscriber = existingSubscriber.get();
            if (subscriber.getIsActive()) {
                response.put("success", false);
                response.put("message", "This email is already subscribed");
                return ResponseEntity.badRequest().body(response);
            } else {
                subscriber.setIsActive(true);
                subscriberRepository.save(subscriber);
                response.put("success", true);
                response.put("message", "Subscriber reactivated successfully");
                response.put("subscriber", subscriber);
                return ResponseEntity.ok(response);
            }
        }

        NewsletterSubscriber newSubscriber = new NewsletterSubscriber();
        newSubscriber.setEmail(email);
        newSubscriber.setIsActive(true);
        NewsletterSubscriber savedSubscriber = subscriberRepository.save(newSubscriber);
        
        response.put("success", true);
        response.put("message", "Subscriber added successfully");
        response.put("subscriber", savedSubscriber);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/admin/{id}")
    public ResponseEntity<Map<String, Object>> deleteSubscriber(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        if (subscriberRepository.existsById(id)) {
            subscriberRepository.deleteById(id);
            response.put("success", true);
            response.put("message", "Subscriber deleted successfully");
            return ResponseEntity.ok(response);
        }
        
        response.put("success", false);
        response.put("message", "Subscriber not found");
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/admin/{id}/deactivate")
    public ResponseEntity<Map<String, Object>> deactivateSubscriber(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        Optional<NewsletterSubscriber> optionalSubscriber = subscriberRepository.findById(id);
        if (optionalSubscriber.isPresent()) {
            NewsletterSubscriber subscriber = optionalSubscriber.get();
            subscriber.setIsActive(false);
            subscriberRepository.save(subscriber);
            response.put("success", true);
            response.put("message", "Subscriber deactivated successfully");
            response.put("subscriber", subscriber);
            return ResponseEntity.ok(response);
        }
        
        response.put("success", false);
        response.put("message", "Subscriber not found");
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/admin/{id}/activate")
    public ResponseEntity<Map<String, Object>> activateSubscriber(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        Optional<NewsletterSubscriber> optionalSubscriber = subscriberRepository.findById(id);
        if (optionalSubscriber.isPresent()) {
            NewsletterSubscriber subscriber = optionalSubscriber.get();
            subscriber.setIsActive(true);
            subscriberRepository.save(subscriber);
            response.put("success", true);
            response.put("message", "Subscriber activated successfully");
            response.put("subscriber", subscriber);
            return ResponseEntity.ok(response);
        }
        
        response.put("success", false);
        response.put("message", "Subscriber not found");
        return ResponseEntity.notFound().build();
    }
}

