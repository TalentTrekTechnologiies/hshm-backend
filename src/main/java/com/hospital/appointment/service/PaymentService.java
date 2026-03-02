package com.hospital.appointment.service;

import com.hospital.appointment.entity.Payment;

public interface PaymentService {

	Payment createOrReuseOrder(Long appointmentId);

	Payment verifyPayment(String orderId,
	                      String transactionId,
	                      String signature);

	Payment markPaymentFailed(String orderId);
}
