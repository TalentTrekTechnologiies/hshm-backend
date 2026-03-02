package com.hospital.appointment.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.hospital.appointment.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // 🔹 Find by Order ID (used during verification)
    Optional<Payment> findByOrderId(String orderId);

    // 🔹 Find by Transaction ID (extra safety / idempotency)
    Optional<Payment> findByTransactionId(String transactionId);

    // 🔹 Find payment linked to appointment
    Optional<Payment> findByAppointmentId(Long appointmentId);

    // 🔹 Scheduler: Find expired active payments
    @Query("""
        SELECT p FROM Payment p
        WHERE p.status NOT IN ('SUCCESS', 'FAILED', 'EXPIRED')
        AND p.orderExpiryTime < :now
    """)
    List<Payment> findExpiredActivePayments(LocalDateTime now);
}
