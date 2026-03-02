package com.hospital.appointment.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.hospital.appointment.enums.PaymentStatus;

import jakarta.persistence.*;

@Entity
@Table(name = "payments",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_payment_order", columnNames = "order_id"),
           @UniqueConstraint(name = "uk_payment_txn", columnNames = "transaction_id")
       },
       indexes = {
           @Index(name = "idx_payment_status", columnList = "status"),
           @Index(name = "idx_payment_expiry", columnList = "order_expiry_time")
       })
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔹 Linked strictly to Appointment
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment;

    // 🔹 Razorpay Order ID
    @Column(name = "order_id", nullable = false, length = 100)
    private String orderId;

    // 🔹 Razorpay Payment ID (Transaction ID)
    @Column(name = "transaction_id", length = 100)
    private String transactionId;

    // 🔹 Razorpay Signature
    @Column(name = "signature", length = 200)
    private String signature;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    // 🔹 Retry attempt counter
    @Column(name = "attempt_number", nullable = false)
    private Integer attemptNumber;

    // 🔹 Order expiry time (for timeout handling)
    @Column(name = "order_expiry_time", nullable = false)
    private LocalDateTime orderExpiryTime;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.attemptNumber == null) {
            this.attemptNumber = 1;
        }
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ================= GETTERS & SETTERS =================

    public Long getId() {
        return id;
    }

    public Appointment getAppointment() {
        return appointment;
    }

    public void setAppointment(Appointment appointment) {
        this.appointment = appointment;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public Integer getAttemptNumber() {
        return attemptNumber;
    }

    public void setAttemptNumber(Integer attemptNumber) {
        this.attemptNumber = attemptNumber;
    }

    public LocalDateTime getOrderExpiryTime() {
        return orderExpiryTime;
    }

    public void setOrderExpiryTime(LocalDateTime orderExpiryTime) {
        this.orderExpiryTime = orderExpiryTime;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}