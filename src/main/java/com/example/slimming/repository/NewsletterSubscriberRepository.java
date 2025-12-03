package com.example.slimming.repository;

import com.example.slimming.entity.NewsletterSubscriber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NewsletterSubscriberRepository extends JpaRepository<NewsletterSubscriber, Long> {
    Optional<NewsletterSubscriber> findByEmail(String email);
    List<NewsletterSubscriber> findByIsActiveTrue();
    List<NewsletterSubscriber> findAllByOrderBySubscribedAtDesc();
}

