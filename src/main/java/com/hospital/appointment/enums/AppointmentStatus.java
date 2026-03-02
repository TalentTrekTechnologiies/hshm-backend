package com.hospital.appointment.enums;

public enum AppointmentStatus {

    // User selected slot, payment not completed yet
    PENDING_PAYMENT,

    // Payment initiated, waiting for gateway confirmation
    PAYMENT_PROCESSING,

    // Payment verified successfully
    CONFIRMED,

    // Payment failed from gateway
    FAILED,

    // User or admin cancelled booking
    CANCELLED,

    // Payment expired due to timeout
    EXPIRED,

    // Refund requested but not completed
    REFUND_PENDING,

    // Refund completed successfully
    REFUND_COMPLETED,
    
    CANCELLATION_REQUESTED
}
