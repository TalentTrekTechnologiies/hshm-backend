package com.hospital.appointment.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hospital.appointment.entity.Appointment;
import com.hospital.appointment.entity.Payment;
import com.hospital.appointment.entity.PaymentConfig;
import com.hospital.appointment.enums.AppointmentStatus;
import com.hospital.appointment.enums.PaymentStatus;
import com.hospital.appointment.exception.BadRequestException;
import com.hospital.appointment.exception.ResourceNotFoundException;
import com.hospital.appointment.repository.AppointmentRepository;
import com.hospital.appointment.repository.PaymentRepository;
import com.hospital.appointment.repository.PaymentConfigRepository;
import com.hospital.appointment.service.AppointmentService;
import com.hospital.appointment.service.AuditLogService;
import com.hospital.appointment.service.PaymentService;
import com.hospital.appointment.validator.StateTransitionValidator;

@Service
public class PaymentServiceImpl implements PaymentService {

    private static final int MAX_RETRY_LIMIT = 5;
    private static final int PAYMENT_TIMEOUT_MINUTES = 10;

    private final PaymentRepository paymentRepository;
    private final AppointmentRepository appointmentRepository;
    private final AppointmentService appointmentService;
    private final StateTransitionValidator stateValidator;
    private final AuditLogService auditLogService;
    private final PaymentConfigRepository configRepository;

    public PaymentServiceImpl(PaymentRepository paymentRepository,
                              AppointmentRepository appointmentRepository,
                              AppointmentService appointmentService,
                              StateTransitionValidator stateValidator,
                              AuditLogService auditLogService,
                              PaymentConfigRepository configRepository) {

        this.paymentRepository = paymentRepository;
        this.appointmentRepository = appointmentRepository;
        this.appointmentService = appointmentService;
        this.stateValidator = stateValidator;
        this.auditLogService = auditLogService;
        this.configRepository = configRepository;
    }

    // =========================================================
    // 1️⃣ CREATE OR REUSE ORDER
    // =========================================================
    @Override
    @Transactional
    public Payment createOrReuseOrder(Long appointmentId) {

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        if (appointment.getStatus() != AppointmentStatus.PENDING_PAYMENT) {
            throw new BadRequestException("Payment cannot be initiated for current appointment state");
        }

        Payment existing = paymentRepository.findByAppointmentId(appointmentId).orElse(null);
        LocalDateTime now = LocalDateTime.now();

        // 🔹 Reuse existing valid order
        if (existing != null &&
                existing.getStatus() != PaymentStatus.SUCCESS &&
                existing.getStatus() != PaymentStatus.EXPIRED &&
                existing.getOrderExpiryTime().isAfter(now)) {

            auditLogService.log(
                    "PAYMENT_ORDER_REUSED",
                    "PAYMENT",
                    existing.getId(),
                    "SYSTEM",
                    null,
                    "Existing order reused for appointmentId: " + appointmentId
            );

            return existing;
        }

        // 🔹 Retry limit enforcement
        int attempt = 1;
        if (existing != null) {
            attempt = existing.getAttemptNumber() + 1;

            if (attempt > MAX_RETRY_LIMIT) {

                stateValidator.validateAppointmentTransition(
                        appointment.getStatus(),
                        AppointmentStatus.EXPIRED);

                appointment.setStatus(AppointmentStatus.EXPIRED);
                appointmentRepository.save(appointment);

                auditLogService.log(
                        "PAYMENT_MAX_RETRY_EXCEEDED",
                        "APPOINTMENT",
                        appointment.getId(),
                        "SYSTEM",
                        null,
                        "Maximum retry attempts exceeded"
                );

                throw new BadRequestException("Maximum payment attempts exceeded");
            }
        }

        // 🔹 Load payment configuration
        PaymentConfig config = configRepository.findAll()
                .stream()
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Payment configuration not set"));

        if (!config.isPaymentEnabled()) {
            throw new BadRequestException("Online payments are disabled");
        }

        // 🔹 Create new order
        Payment payment = new Payment();
        payment.setAppointment(appointment);
        payment.setAmount(config.getConsultationFee());
        payment.setOrderId("ORDER-" + UUID.randomUUID());
        payment.setStatus(PaymentStatus.INITIATED);
        payment.setAttemptNumber(attempt);
        payment.setOrderExpiryTime(now.plusMinutes(PAYMENT_TIMEOUT_MINUTES));

        stateValidator.validateAppointmentTransition(
                appointment.getStatus(),
                AppointmentStatus.PAYMENT_PROCESSING);

        appointment.setStatus(AppointmentStatus.PAYMENT_PROCESSING);
        appointment.setPaymentStartedAt(now);
        appointmentRepository.save(appointment);

        Payment saved = paymentRepository.save(payment);

        auditLogService.log(
                "PAYMENT_ORDER_CREATED",
                "PAYMENT",
                saved.getId(),
                "SYSTEM",
                null,
                "Order created with OrderId: " + saved.getOrderId()
        );

        return saved;
    }

    // =========================================================
    // 2️⃣ VERIFY PAYMENT
    // =========================================================
    @Override
    @Transactional
    public Payment verifyPayment(String orderId,
                                 String transactionId,
                                 String signature) {

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            return payment; // Idempotent
        }

        if (payment.getStatus() == PaymentStatus.EXPIRED) {
            throw new BadRequestException("Order already expired");
        }

        Appointment appt = payment.getAppointment();

        if (appt.getStatus() == AppointmentStatus.EXPIRED) {
            throw new BadRequestException("Appointment already expired");
        }

        if (payment.getOrderExpiryTime().isBefore(LocalDateTime.now())) {

            stateValidator.validatePaymentTransition(
                    payment.getStatus(),
                    PaymentStatus.EXPIRED);

            payment.setStatus(PaymentStatus.EXPIRED);
            paymentRepository.save(payment);

            stateValidator.validateAppointmentTransition(
                    appt.getStatus(),
                    AppointmentStatus.EXPIRED);

            appt.setStatus(AppointmentStatus.EXPIRED);
            appointmentRepository.save(appt);

            auditLogService.log(
                    "PAYMENT_EXPIRED",
                    "PAYMENT",
                    payment.getId(),
                    "SYSTEM",
                    null,
                    "Payment expired due to timeout"
            );

            throw new BadRequestException("Payment expired");
        }

        stateValidator.validatePaymentTransition(
                payment.getStatus(),
                PaymentStatus.SUCCESS);

        payment.setTransactionId(transactionId);
        payment.setSignature(signature);
        payment.setStatus(PaymentStatus.SUCCESS);
        paymentRepository.save(payment);

        auditLogService.log(
                "PAYMENT_SUCCESS",
                "PAYMENT",
                payment.getId(),
                "SYSTEM",
                null,
                "Payment verified. OrderId: " + orderId + ", TxnId: " + transactionId
        );

        appointmentService.confirmAppointment(payment.getId());

        return payment;
    }

    // =========================================================
    // 3️⃣ MARK PAYMENT FAILED
    // =========================================================
    @Override
    @Transactional
    public Payment markPaymentFailed(String orderId) {

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            throw new BadRequestException("Payment already successful");
        }

        stateValidator.validatePaymentTransition(
                payment.getStatus(),
                PaymentStatus.FAILED);

        payment.setStatus(PaymentStatus.FAILED);
        paymentRepository.save(payment);

        Appointment appointment = payment.getAppointment();

        stateValidator.validateAppointmentTransition(
                appointment.getStatus(),
                AppointmentStatus.FAILED);

        appointment.setStatus(AppointmentStatus.FAILED);
        appointmentRepository.save(appointment);

        auditLogService.log(
                "PAYMENT_FAILED",
                "PAYMENT",
                payment.getId(),
                "SYSTEM",
                null,
                "Payment marked FAILED for OrderId: " + orderId
        );

        return payment;
    }
}