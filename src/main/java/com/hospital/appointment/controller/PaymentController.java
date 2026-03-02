package com.hospital.appointment.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.hospital.appointment.entity.Payment;
import com.hospital.appointment.service.PaymentService;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // =========================================================
    // 1️⃣ CREATE OR REUSE ORDER
    // =========================================================
    @PostMapping("/order")
    public ResponseEntity<?> createOrder(@RequestParam Long appointmentId) {

        Payment payment = paymentService.createOrReuseOrder(appointmentId);

        return ResponseEntity.ok(Map.of(
                "message", "Order created or reused",
                "paymentId", payment.getId(),
                "orderId", payment.getOrderId(),
                "status", payment.getStatus(),
                "amount", payment.getAmount(),
                "expiryTime", payment.getOrderExpiryTime()
        ));
    }

    // =========================================================
    // 2️⃣ VERIFY PAYMENT (Called after gateway success)
    // =========================================================
    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(
            @RequestParam String orderId,
            @RequestParam String transactionId,
            @RequestParam String signature) {

        Payment payment = paymentService.verifyPayment(orderId, transactionId, signature);

        return ResponseEntity.ok(Map.of(
                "message", "Payment verified",
                "paymentId", payment.getId(),
                "status", payment.getStatus()
        ));
    }

    // =========================================================
    // 3️⃣ MARK FAILED
    // =========================================================
    @PostMapping("/failed")
    public ResponseEntity<?> markFailed(@RequestParam String orderId) {

        Payment payment = paymentService.markPaymentFailed(orderId);

        return ResponseEntity.ok(Map.of(
                "message", "Payment marked failed",
                "paymentId", payment.getId(),
                "status", payment.getStatus()
        ));
    }
}