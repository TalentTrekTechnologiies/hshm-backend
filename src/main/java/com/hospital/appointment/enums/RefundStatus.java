package com.hospital.appointment.enums;

public enum RefundStatus {

    // Refund created but not yet completed in Razorpay
    REFUND_PENDING,

    // Refund successfully processed
    REFUND_COMPLETED,

    // Refund attempt failed
    REFUND_FAILED
}
