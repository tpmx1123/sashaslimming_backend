package com.example.slimming.repository;

import com.example.slimming.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByOrderByCreatedAtDesc();
    List<Booking> findByIsReadFalseOrderByCreatedAtDesc();
}

