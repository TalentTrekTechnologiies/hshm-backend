package com.hospital.appointment.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hospital.appointment.entity.Enquiry;
import com.hospital.appointment.enums.EnquiryStatus;

public interface EnquiryRepository extends JpaRepository<Enquiry, Long> {

    // =========================================================
    // ADMIN FILTERS
    // =========================================================

    // Admin can filter enquiries by status (latest first)
    List<Enquiry> findByStatusOrderByCreatedAtDesc(EnquiryStatus status);

    // Admin can view all enquiries (latest first)
    List<Enquiry> findAllByOrderByCreatedAtDesc();

    // =========================================================
    // SPAM / DUPLICATE PREVENTION
    // =========================================================

    // Basic spam prevention: same phone already submitted
    boolean existsByPhone(String phone);

    // Better spam prevention: same phone submitted recently (optional)
    boolean existsByPhoneAndCreatedAtAfter(String phone, LocalDateTime afterTime);

    // Optional: prevent spam by email also
    boolean existsByEmailAndCreatedAtAfter(String email, LocalDateTime afterTime);
}


