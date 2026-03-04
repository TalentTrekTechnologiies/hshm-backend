package com.hospital.appointment.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hospital.appointment.entity.Appointment;
import com.hospital.appointment.entity.Payment;
import com.hospital.appointment.entity.Slot;
import com.hospital.appointment.enums.AppointmentStatus;
import com.hospital.appointment.enums.PaymentStatus;
import com.hospital.appointment.enums.SlotStatus;
import com.hospital.appointment.exception.BadRequestException;
import com.hospital.appointment.exception.ResourceNotFoundException;
import com.hospital.appointment.repository.AppointmentRepository;
import com.hospital.appointment.repository.PaymentRepository;
import com.hospital.appointment.repository.SlotRepository;
import com.hospital.appointment.service.AppointmentService;
import com.hospital.appointment.service.AuditLogService;
import com.hospital.appointment.service.NotificationService;
import com.hospital.appointment.service.RefundService;
import com.hospital.appointment.validator.StateTransitionValidator;

@Service
public class AppointmentServiceImpl implements AppointmentService {

    private static final int RESCHEDULE_CUTOFF_MINUTES = 120;

    private final AppointmentRepository appointmentRepository;
    private final PaymentRepository paymentRepository;
    private final SlotRepository slotRepository;
    private final NotificationService notificationService;
    private final RefundService refundService;
    private final StateTransitionValidator stateValidator;
    private final AuditLogService auditLogService;

    public AppointmentServiceImpl(AppointmentRepository appointmentRepository,
                                  PaymentRepository paymentRepository,
                                  SlotRepository slotRepository,
                                  NotificationService notificationService,
                                  RefundService refundService,
                                  StateTransitionValidator stateValidator,
                                  AuditLogService auditLogService) {

        this.appointmentRepository = appointmentRepository;
        this.paymentRepository = paymentRepository;
        this.slotRepository = slotRepository;
        this.notificationService = notificationService;
        this.refundService = refundService;
        this.stateValidator = stateValidator;
        this.auditLogService = auditLogService;
    }

    // =========================================================
    // 1️⃣ CONFIRM APPOINTMENT AFTER PAYMENT
    // =========================================================
    @Override
    @Transactional
    public Appointment confirmAppointment(Long paymentId) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new BadRequestException("Payment must be SUCCESS to confirm appointment");
        }

        Appointment appointment = payment.getAppointment();

        if (appointment == null) {
            throw new BadRequestException("Appointment not linked to payment");
        }

        if (appointment.getStatus() == AppointmentStatus.CONFIRMED) {
            return appointment;
        }

        stateValidator.validateAppointmentTransition(
                appointment.getStatus(),
                AppointmentStatus.CONFIRMED
        );

        appointment.setStatus(AppointmentStatus.CONFIRMED);

        Slot slot = appointment.getSlot();

        if (slot == null) {
            throw new BadRequestException("Slot not found for appointment");
        }

        slot.setStatus(SlotStatus.BOOKED);
        slot.setLockedBy(null);
        slot.setLockUntil(null);

        slotRepository.save(slot);

        Appointment saved = appointmentRepository.save(appointment);

        auditLogService.log(
                "APPOINTMENT_CONFIRMED",
                "APPOINTMENT",
                saved.getId(),
                "SYSTEM",
                null,
                "Appointment confirmed after successful payment"
        );

        try {
            notificationService.sendBookingConfirmed(saved);
        } catch (Exception ignored) {}

        return saved;
    }

    // =========================================================
    // 2️⃣ PATIENT REQUEST CANCELLATION
    // =========================================================
    @Override
    @Transactional
    public Appointment cancelAppointment(Long appointmentId, String reason) {

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        if (appointment.getStatus() == AppointmentStatus.CANCELLATION_REQUESTED) {
            return appointment;
        }

        stateValidator.validateAppointmentTransition(
                appointment.getStatus(),
                AppointmentStatus.CANCELLATION_REQUESTED
        );

        appointment.setStatus(AppointmentStatus.CANCELLATION_REQUESTED);

        Appointment saved = appointmentRepository.save(appointment);

        auditLogService.log(
                "CANCELLATION_REQUESTED",
                "APPOINTMENT",
                saved.getId(),
                "PATIENT",
                null,
                "Reason: " + (reason == null ? "N/A" : reason)
        );

        return saved;
    }

    // =========================================================
    // 3️⃣ ADMIN APPROVES CANCELLATION
    // =========================================================
    @Transactional
    public Appointment approveCancellation(Long appointmentId) {

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        if (appointment.getStatus() != AppointmentStatus.CANCELLATION_REQUESTED) {
            throw new BadRequestException("Cancellation not requested");
        }

        stateValidator.validateAppointmentTransition(
                appointment.getStatus(),
                AppointmentStatus.CANCELLED
        );

        appointment.setStatus(AppointmentStatus.CANCELLED);

        Slot slot = appointment.getSlot();

        if (slot != null) {
            slot.setStatus(SlotStatus.AVAILABLE);
            slot.setLockedBy(null);
            slot.setLockUntil(null);
            slotRepository.save(slot);
        }

        Appointment saved = appointmentRepository.save(appointment);

        Payment payment = paymentRepository.findByAppointmentId(saved.getId()).orElse(null);

        if (payment != null && payment.getStatus() == PaymentStatus.SUCCESS) {

            stateValidator.validateAppointmentTransition(
                    saved.getStatus(),
                    AppointmentStatus.REFUND_PENDING
            );

            saved.setStatus(AppointmentStatus.REFUND_PENDING);
            appointmentRepository.save(saved);

            refundService.initiateRefund(payment.getId(), "Approved by hospital");
        }

        auditLogService.log(
                "CANCELLATION_APPROVED",
                "APPOINTMENT",
                saved.getId(),
                "ADMIN",
                null,
                "Slot released and refund initiated"
        );

        return saved;
    }

    // =========================================================
    // 4️⃣ RESCHEDULE APPOINTMENT
    // =========================================================
    @Transactional
    public Appointment rescheduleAppointment(Long appointmentId,
                                             Long newSlotId,
                                             Long patientId) {

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        if (!appointment.getPatient().getId().equals(patientId)) {
            throw new BadRequestException("Unauthorized reschedule attempt");
        }

        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new BadRequestException("Only confirmed appointments can be rescheduled");
        }

        Slot currentSlot = appointment.getSlot();

        LocalDateTime appointmentDateTime =
                LocalDateTime.of(currentSlot.getSlotDate(), currentSlot.getStartTime());

        if (LocalDateTime.now()
                .isAfter(appointmentDateTime.minusMinutes(RESCHEDULE_CUTOFF_MINUTES))) {
            throw new BadRequestException("Reschedule window closed");
        }

        int locked = slotRepository.lockSlot(
                newSlotId,
                patientId,
                LocalDateTime.now().plusMinutes(10)
        );

        if (locked == 0) {
            throw new BadRequestException("New slot unavailable");
        }

        Slot newSlot = slotRepository.findById(newSlotId)
                .orElseThrow(() -> new ResourceNotFoundException("Slot not found"));

        currentSlot.setStatus(SlotStatus.AVAILABLE);
        currentSlot.setLockedBy(null);
        currentSlot.setLockUntil(null);
        slotRepository.save(currentSlot);

        newSlot.setStatus(SlotStatus.BOOKED);
        newSlot.setLockedBy(null);
        newSlot.setLockUntil(null);
        slotRepository.save(newSlot);

        appointment.setSlot(newSlot);

        Appointment saved = appointmentRepository.save(appointment);

        auditLogService.log(
                "APPOINTMENT_RESCHEDULED",
                "APPOINTMENT",
                saved.getId(),
                "PATIENT",
                null,
                "Rescheduled successfully"
        );

        return saved;
    }

    // =========================================================
    // 5️⃣ HISTORY APIs
    // =========================================================

    @Override
    public List<Appointment> getPatientAppointments(Long patientId) {
        return appointmentRepository.findByPatientIdWithDetails(patientId);
    }

    @Override
    public List<Appointment> getDoctorAppointments(Long doctorId) {
        return appointmentRepository.findByDoctorIdWithDetails(doctorId);
    }
    @Override
    public List<Appointment> getAppointmentsByStatus(AppointmentStatus status) {
        return appointmentRepository.findByStatus(status);
    }
}