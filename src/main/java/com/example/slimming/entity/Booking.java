package com.example.slimming.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String serviceName;

    @Column(nullable = false)
    private String date;

    @Column(nullable = false)
    private String time;

    @Column(length = 2000)
    private String message;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private Boolean isRead = false;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (isRead == null) {
            isRead = false;
        }
    }
}

