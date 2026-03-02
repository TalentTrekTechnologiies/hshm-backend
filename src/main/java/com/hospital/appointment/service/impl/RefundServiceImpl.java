package com.hospital.appointment.service.impl;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hospital.appointment.entity.Appointment;
import com.hospital.appointment.entity.Payment;
import com.hospital.appointment.entity.Refund;
import com.hospital.appointment.enums.AppointmentStatus;
import com.hospital.appointment.enums.PaymentStatus;
import com.hospital.appointment.enums.RefundStatus;
import com.hospital.appointment.exception.BadRequestException;
import com.hospital.appointment.exception.ResourceNotFoundException;
import com.hospital.appointment.repository.AppointmentRepository;
import com.hospital.appointment.repository.PaymentRepository;
import com.hospital.appointment.repository.RefundRepository;
import com.hospital.appointment.service.AuditLogService;
import com.hospital.appointment.service.RefundService;
import com.hospital.appointment.validator.StateTransitionValidator;

@Service
public class RefundServiceImpl implements RefundService {

    private final RefundRepository refundRepository;
    private final PaymentRepository paymentRepository;
    private final AppointmentRepository appointmentRepository;
    private final StateTransitionValidator stateValidator;
    private final AuditLogService auditLogService;

    public RefundServiceImpl(RefundRepository refundRepository,
                             PaymentRepository paymentRepository,
                             AppointmentRepository appointmentRepository,
                             StateTransitionValidator stateValidator,
                             AuditLogService auditLogService) {

        this.refundRepository = refundRepository;
        this.paymentRepository = paymentRepository;
        this.appointmentRepository = appointmentRepository;
        this.stateValidator = stateValidator;
        this.auditLogService = auditLogService;
    }

    // =========================================================
    // 1️⃣ INITIATE REFUND
    // =========================================================
    @Override
    @Transactional
    public Refund initiateRefund(Long paymentId, String reason) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new BadRequestException("Refund allowed only for SUCCESS payments");
        }

        // Idempotent check
        Refund existing = refundRepository.findByPaymentId(paymentId).orElse(null);
        if (existing != null) {
            return existing;
        }

        Appointment appointment = payment.getAppointment();

        stateValidator.validateAppointmentTransition(
                appointment.getStatus(),
                AppointmentStatus.REFUND_PENDING);

        appointment.setStatus(AppointmentStatus.REFUND_PENDING);
        appointmentRepository.save(appointment);

        Refund refund = new Refund();
        refund.setPayment(payment);
        refund.setAmount(payment.getAmount());
        refund.setReason(reason);
        refund.setStatus(RefundStatus.REFUND_PENDING);
        refund.setGatewayRefundId("REF-" + UUID.randomUUID());

        Refund saved = refundRepository.save(refund);

        auditLogService.log(
                "REFUND_INITIATED",
                "REFUND",
                saved.getId(),
                "SYSTEM",
                null,
                "Refund initiated for PaymentId: " + paymentId
        );

        return saved;
    }

    // =========================================================
    // 2️⃣ MARK REFUND COMPLETED
    // =========================================================
    @Override
    @Transactional
    public Refund markRefundSuccess(Long refundId) {

        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new ResourceNotFoundException("Refund not found"));

        if (refund.getStatus() == RefundStatus.REFUND_COMPLETED) {
            return refund; // idempotent
        }

        if (refund.getStatus() != RefundStatus.REFUND_PENDING) {
            throw new BadRequestException("Refund must be REFUND_PENDING to complete");
        }

        refund.setStatus(RefundStatus.REFUND_COMPLETED);
        Refund saved = refundRepository.save(refund);

        Appointment appointment = refund.getPayment().getAppointment();

        stateValidator.validateAppointmentTransition(
                appointment.getStatus(),
                AppointmentStatus.REFUND_COMPLETED);

        appointment.setStatus(AppointmentStatus.REFUND_COMPLETED);
        appointmentRepository.save(appointment);

        auditLogService.log(
                "REFUND_COMPLETED",
                "REFUND",
                saved.getId(),
                "SYSTEM",
                null,
                "Refund completed successfully"
        );

        return saved;
    }

    // =========================================================
    // 3️⃣ MARK REFUND FAILED
    // =========================================================
    @Override
    @Transactional
    public Refund markRefundFailed(Long refundId, String errorReason) {

        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new ResourceNotFoundException("Refund not found"));

        if (refund.getStatus() == RefundStatus.REFUND_FAILED) {
            return refund; // idempotent
        }

        if (refund.getStatus() == RefundStatus.REFUND_COMPLETED) {
            throw new BadRequestException("Refund already completed");
        }

        refund.setStatus(RefundStatus.REFUND_FAILED);

        if (errorReason != null && !errorReason.isBlank()) {
            refund.setReason(errorReason);
        }

        Refund saved = refundRepository.save(refund);

        auditLogService.log(
                "REFUND_FAILED",
                "REFUND",
                saved.getId(),
                "SYSTEM",
                null,
                "Refund failed. Reason: " + (errorReason == null ? "N/A" : errorReason)
        );

        return saved;
    }

    // =========================================================
    // 4️⃣ RETRY REFUND
    // =========================================================
    @Override
    @Transactional
    public Refund retryRefund(Long refundId) {

        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new ResourceNotFoundException("Refund not found"));

        if (refund.getStatus() == RefundStatus.REFUND_COMPLETED) {
            return refund;
        }

        if (refund.getStatus() != RefundStatus.REFUND_FAILED) {
            throw new BadRequestException("Only REFUND_FAILED can be retried");
        }

        refund.setStatus(RefundStatus.REFUND_PENDING);
        refund.setGatewayRefundId("REF-RETRY-" + UUID.randomUUID());

        Refund saved = refundRepository.save(refund);

        auditLogService.log(
                "REFUND_RETRIED",
                "REFUND",
                saved.getId(),
                "SYSTEM",
                null,
                "Refund retry initiated"
        );

        return saved;
    }
}