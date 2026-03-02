package com.hospital.appointment.controller;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.appointment.service.AuditLogService;
import com.hospital.appointment.service.PaymentService;

@RestController
@RequestMapping("/api/payments")
public class RazorpayWebhookController {

    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;
    private final AuditLogService auditLogService;

    @Value("${razorpay.webhook.secret}")
    private String webhookSecret;

    public RazorpayWebhookController(PaymentService paymentService,
                                     ObjectMapper objectMapper,
                                     AuditLogService auditLogService) {
        this.paymentService = paymentService;
        this.objectMapper = objectMapper;
        this.auditLogService = auditLogService;
    }

    // =========================================================
    // RAZORPAY WEBHOOK ENDPOINT
    // =========================================================
    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestHeader(value = "X-Razorpay-Signature", required = false) String razorpaySignature,
            @RequestBody String payload) {

        try {

            auditLogService.log(
                    "WEBHOOK_RECEIVED",
                    "PAYMENT",
                    0L,
                    "RAZORPAY",
                    null,
                    "Webhook received"
            );

            // 1️⃣ Signature must exist
            if (razorpaySignature == null || razorpaySignature.isBlank()) {

                auditLogService.log(
                        "WEBHOOK_MISSING_SIGNATURE",
                        "PAYMENT",
                        0L,
                        "RAZORPAY",
                        null,
                        "Missing signature header"
                );

                return ResponseEntity.badRequest().body("Missing signature");
            }

            // 2️⃣ Verify webhook signature
            if (!verifyWebhookSignature(payload, razorpaySignature, webhookSecret)) {

                auditLogService.log(
                        "WEBHOOK_INVALID_SIGNATURE",
                        "PAYMENT",
                        0L,
                        "RAZORPAY",
                        null,
                        "Invalid webhook signature"
                );

                return ResponseEntity.badRequest().body("Invalid signature");
            }

            // 3️⃣ Parse JSON safely
            JsonNode root = objectMapper.readTree(payload);
            String eventType = root.path("event").asText();

            // Only process payment.captured
            if (!"payment.captured".equals(eventType)) {

                auditLogService.log(
                        "WEBHOOK_EVENT_IGNORED",
                        "PAYMENT",
                        0L,
                        "RAZORPAY",
                        null,
                        "Ignored event type: " + eventType
                );

                return ResponseEntity.ok("Event ignored");
            }

            JsonNode paymentEntity = root
                    .path("payload")
                    .path("payment")
                    .path("entity");

            String orderId = paymentEntity.path("order_id").asText();
            String paymentId = paymentEntity.path("id").asText();

            if (orderId.isBlank() || paymentId.isBlank()) {

                auditLogService.log(
                        "WEBHOOK_INVALID_PAYLOAD",
                        "PAYMENT",
                        0L,
                        "RAZORPAY",
                        null,
                        "Missing orderId or paymentId"
                );

                return ResponseEntity.badRequest().body("Invalid payload structure");
            }

            // Call service (idempotent internally)
            paymentService.verifyPayment(orderId, paymentId, null);

            auditLogService.log(
                    "WEBHOOK_PAYMENT_CAPTURED",
                    "PAYMENT",
                    0L,
                    "RAZORPAY",
                    null,
                    "Payment captured for OrderId: " + orderId
            );

            return ResponseEntity.ok("Webhook processed successfully");

        } catch (Exception ex) {

            auditLogService.log(
                    "WEBHOOK_PROCESSING_ERROR",
                    "PAYMENT",
                    0L,
                    "RAZORPAY",
                    null,
                    "Webhook processing error"
            );

            return ResponseEntity.internalServerError().body("Webhook processing error");
        }
    }

    // =========================================================
    // TIMING SAFE SIGNATURE VALIDATION
    // =========================================================
    private boolean verifyWebhookSignature(String payload,
                                           String signature,
                                           String secret) throws Exception {

        Mac sha256Hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256");

        sha256Hmac.init(secretKey);

        byte[] hash = sha256Hmac.doFinal(payload.getBytes(StandardCharsets.UTF_8));

        String generatedSignature = bytesToHex(hash);

        return MessageDigest.isEqual(
                generatedSignature.getBytes(StandardCharsets.UTF_8),
                signature.getBytes(StandardCharsets.UTF_8));
    }

    private String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}