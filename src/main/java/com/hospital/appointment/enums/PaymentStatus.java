package com.hospital.appointment.enums;

public enum PaymentStatus {

    // Order created but payment not yet attempted
    INITIATED,

    // User is inside Razorpay / bank processing
    PROCESSING,

    // Payment verified successfully
    SUCCESS,

    // Bank or gateway declined payment
    FAILED,

    // User closed popup or cancelled before completion
    CANCELLED,

    // Order expired due to timeout
    EXPIRED
}