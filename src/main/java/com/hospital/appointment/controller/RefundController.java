package com.hospital.appointment.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.hospital.appointment.entity.Refund;
import com.hospital.appointment.service.RefundService;

@RestController
@RequestMapping("/api/refunds")
@CrossOrigin
public class RefundController {

    private final RefundService refundService;

    public RefundController(RefundService refundService) {
        this.refundService = refundService;
    }

    // 1) Initiate refund (idempotent)
    @PostMapping("/initiate")
    public ResponseEntity<?> initiate(@RequestParam Long paymentId,
                                      @RequestParam(required = false, defaultValue = "Refund initiated") String reason) {

        Refund refund = refundService.initiateRefund(paymentId, reason);

        return ResponseEntity.ok(Map.of(
                "message", "Refund initiated",
                "refundId", refund.getId(),
                "paymentId", paymentId,
                "status", refund.getStatus(),
                "amount", refund.getAmount(),
                "gatewayRefundId", refund.getGatewayRefundId()
        ));
    }

    // 2) Sandbox: mark refund success
    @PostMapping("/{refundId}/success")
    public ResponseEntity<?> success(@PathVariable Long refundId) {

        Refund refund = refundService.markRefundSuccess(refundId);

        return ResponseEntity.ok(Map.of(
                "message", "Refund marked SUCCESS",
                "refundId", refund.getId(),
                "status", refund.getStatus()
        ));
    }

    // 3) Sandbox: mark refund failed
    @PostMapping("/{refundId}/failed")
    public ResponseEntity<?> failed(@PathVariable Long refundId,
                                    @RequestParam(required = false, defaultValue = "Gateway failed") String reason) {

        Refund refund = refundService.markRefundFailed(refundId, reason);

        return ResponseEntity.ok(Map.of(
                "message", "Refund marked FAILED",
                "refundId", refund.getId(),
                "status", refund.getStatus(),
                "reason", refund.getReason()
        ));
    }

    // 4) Retry
    @PostMapping("/{refundId}/retry")
    public ResponseEntity<?> retry(@PathVariable Long refundId) {

        Refund refund = refundService.retryRefund(refundId);

        return ResponseEntity.ok(Map.of(
                "message", "Refund retry initiated",
                "refundId", refund.getId(),
                "status", refund.getStatus(),
                "gatewayRefundId", refund.getGatewayRefundId()
        ));
    }
}
