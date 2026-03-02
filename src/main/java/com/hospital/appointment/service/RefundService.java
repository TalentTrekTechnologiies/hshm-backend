package com.hospital.appointment.service;

import com.hospital.appointment.entity.Refund;

public interface RefundService {

    // create refund for a payment (idempotent)
    Refund initiateRefund(Long paymentId, String reason);

    // sandbox simulation
    Refund markRefundSuccess(Long refundId);

    Refund markRefundFailed(Long refundId, String errorReason);

    // retry if FAILED
    Refund retryRefund(Long refundId);
}
