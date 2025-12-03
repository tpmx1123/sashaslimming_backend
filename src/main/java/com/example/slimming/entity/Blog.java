package com.example.slimming.entity;

import com.example.slimming.enums.BlogStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "blogs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Blog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String excerpt;

    @Column(nullable = false)
    private String image;

    @Column(nullable = false)
    private String category;

    @Column(nullable = true, unique = true)
    private String slug;

    @Column(nullable = true, columnDefinition = "TEXT")
    private String metaDescription;

    @Column(nullable = true)
    private String tags; // Comma-separated tags

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private Boolean isFeatured = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BlogStatus status = BlogStatus.DRAFT;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = BlogStatus.DRAFT;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
