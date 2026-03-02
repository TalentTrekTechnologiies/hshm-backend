package com.hospital.appointment.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.hospital.appointment.entity.Appointment;
import com.hospital.appointment.entity.Payment;
import com.hospital.appointment.entity.Slot;
import com.hospital.appointment.enums.AppointmentStatus;
import com.hospital.appointment.enums.PaymentStatus;
import com.hospital.appointment.enums.SlotStatus;
import com.hospital.appointment.repository.AppointmentRepository;
import com.hospital.appointment.repository.PaymentRepository;
import com.hospital.appointment.repository.SlotRepository;

@Component
public class PaymentExpiryScheduler {

    private final PaymentRepository paymentRepository;
    private final AppointmentRepository appointmentRepository;
    private final SlotRepository slotRepository;

    public PaymentExpiryScheduler(PaymentRepository paymentRepository,
                                   AppointmentRepository appointmentRepository,
                                   SlotRepository slotRepository) {
        this.paymentRepository = paymentRepository;
        this.appointmentRepository = appointmentRepository;
        this.slotRepository = slotRepository;
    }

 // Runs every 1 minute
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void expirePayments() {

        LocalDateTime now = LocalDateTime.now();

        List<Payment> expiredPayments =
                paymentRepository.findExpiredActivePayments(now);

        for (Payment payment : expiredPayments) {

            // 1️⃣ Mark payment as EXPIRED
            payment.setStatus(PaymentStatus.EXPIRED);
            paymentRepository.save(payment);

            Appointment appointment = payment.getAppointment();

            // 🔒 Only expire appointments still waiting for payment
            if (appointment != null &&
                    appointment.getStatus() == AppointmentStatus.PAYMENT_PROCESSING) {

                // 2️⃣ Mark appointment as EXPIRED
                appointment.setStatus(AppointmentStatus.EXPIRED);
                appointmentRepository.save(appointment);

                // 3️⃣ Release slot ONLY if still LOCKED
                Slot slot = appointment.getSlot();

                if (slot.getStatus() == SlotStatus.LOCKED) {

                    slot.setStatus(SlotStatus.AVAILABLE);
                    slot.setLockedBy(null);
                    slot.setLockUntil(null);

                    slotRepository.save(slot);
                }
            }
        }
    }
}
    