package com.example.slimming.controller;

import com.example.slimming.entity.Booking;
import com.example.slimming.repository.BookingRepository;
import com.example.slimming.service.EmailService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = {"https://sashaslimming.com", "https://www.sashaslimming.com", "http://localhost:3000", "http://localhost:3001", "http://localhost:5173"})
public class BookingController {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private EmailService emailService;

   

    // Public endpoint - Anyone can submit a booking
    @PostMapping
    public ResponseEntity<Booking> saveBooking(@RequestBody Booking booking) {
        Booking savedBooking = bookingRepository.save(booking);
        
        // Send confirmation emails (to customer and admin)
        try {
            emailService.sendBookingConfirmation(savedBooking);
        } catch (Exception e) {
            System.err.println("Failed to send booking confirmation emails: " + e.getMessage());
            // Don't fail the booking if email fails - booking is already saved
        }
        
      
        
        return ResponseEntity.ok(savedBooking);
    }

    // Admin endpoints - Require JWT authentication (handled by SecurityConfig)
    @GetMapping("/admin/all")
    public ResponseEntity<List<Booking>> getAllBookings() {
        List<Booking> bookings = bookingRepository.findByOrderByCreatedAtDesc();
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/admin/unread")
    public ResponseEntity<List<Booking>> getUnreadBookings() {
        List<Booking> unreadBookings = bookingRepository.findByIsReadFalseOrderByCreatedAtDesc();
        return ResponseEntity.ok(unreadBookings);
    }

    @PutMapping("/admin/{id}/mark-read")
    public ResponseEntity<Booking> markAsRead(@PathVariable Long id) {
        Optional<Booking> optionalBooking = bookingRepository.findById(id);
        if (optionalBooking.isPresent()) {
            Booking booking = optionalBooking.get();
            booking.setIsRead(true);
            Booking updatedBooking = bookingRepository.save(booking);
            return ResponseEntity.ok(updatedBooking);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/admin/{id}")
    public ResponseEntity<?> deleteBooking(@PathVariable Long id) {
        if (bookingRepository.existsById(id)) {
            bookingRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}

