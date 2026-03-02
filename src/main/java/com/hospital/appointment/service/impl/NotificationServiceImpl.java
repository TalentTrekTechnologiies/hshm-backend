package com.hospital.appointment.service.impl;

import java.time.LocalDateTime;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hospital.appointment.entity.Appointment;
import com.hospital.appointment.entity.NotificationLog;
import com.hospital.appointment.entity.Patient;
import com.hospital.appointment.enums.NotificationChannel;
import com.hospital.appointment.enums.NotificationStatus;
import com.hospital.appointment.enums.NotificationType;
import com.hospital.appointment.repository.NotificationLogRepository;
import com.hospital.appointment.service.NotificationService;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationLogRepository notificationLogRepository;
    private final JavaMailSender mailSender;

    public NotificationServiceImpl(NotificationLogRepository notificationLogRepository,
                                   JavaMailSender mailSender) {
        this.notificationLogRepository = notificationLogRepository;
        this.mailSender = mailSender;
    }

    // =========================================================
    // 1) BOOKING CONFIRMED
    // =========================================================
    @Override
    @Transactional
    public void sendBookingConfirmed(Appointment appointment) {

        Patient patient = appointment.getPatient();
        String msg = buildBookingConfirmedMessage(appointment);

        // EMAIL (only if email exists)
        if (patient.getEmail() != null && !patient.getEmail().isBlank()) {
            safeSend(
                    appointment,
                    patient,
                    NotificationType.BOOKING_CONFIRMED,
                    NotificationChannel.EMAIL,
                    patient.getEmail(),
                    "Appointment Confirmed - Harsha Multispeciality Hospital",
                    msg
            );
        }

        // WHATSAPP (sandbox)
        safeSend(
                appointment,
                patient,
                NotificationType.BOOKING_CONFIRMED,
                NotificationChannel.WHATSAPP,
                patient.getPhone(),
                "Appointment Confirmed",
                msg
        );
    }

    // =========================================================
    // 2) BOOKING CANCELLED
    // =========================================================
    @Override
    @Transactional
    public void sendBookingCancelled(Appointment appointment) {

        Patient patient = appointment.getPatient();
        String msg = buildBookingCancelledMessage(appointment);

        // EMAIL (only if email exists)
        if (patient.getEmail() != null && !patient.getEmail().isBlank()) {
            safeSend(
                    appointment,
                    patient,
                    NotificationType.BOOKING_CANCELLED,
                    NotificationChannel.EMAIL,
                    patient.getEmail(),
                    "Appointment Cancelled - Harsha Multispeciality Hospital",
                    msg
            );
        }

        // WHATSAPP (sandbox)
        safeSend(
                appointment,
                patient,
                NotificationType.BOOKING_CANCELLED,
                NotificationChannel.WHATSAPP,
                patient.getPhone(),
                "Appointment Cancelled",
                msg
        );
    }

    // =========================================================
    // 3) BOOKING RESCHEDULED
    // =========================================================
    @Override
    @Transactional
    public void sendBookingRescheduled(Appointment appointment) {

        Patient patient = appointment.getPatient();
        String msg = buildBookingRescheduledMessage(appointment);

        // EMAIL (only if email exists)
        if (patient.getEmail() != null && !patient.getEmail().isBlank()) {
            safeSend(
                    appointment,
                    patient,
                    NotificationType.BOOKING_RESCHEDULED,
                    NotificationChannel.EMAIL,
                    patient.getEmail(),
                    "Appointment Rescheduled - Harsha Multispeciality Hospital",
                    msg
            );
        }

        // WHATSAPP (sandbox)
        safeSend(
                appointment,
                patient,
                NotificationType.BOOKING_RESCHEDULED,
                NotificationChannel.WHATSAPP,
                patient.getPhone(),
                "Appointment Rescheduled",
                msg
        );
    }

    // =========================================================
    // MESSAGE BUILDERS
    // =========================================================
    private String buildBookingConfirmedMessage(Appointment appointment) {

        return "Dear " + appointment.getPatient().getName() + ",\n\n"
                + "Your online consultation appointment is CONFIRMED ✅\n\n"
                + "Hospital: Harsha Multispeciality Hospital\n"
                + "Doctor: " + appointment.getDoctor().getName() + "\n"
                + "Date: " + appointment.getSlot().getSlotDate() + "\n"
                + "Time: " + appointment.getSlot().getStartTime() + "\n\n"
                + "Consultation will happen via WhatsApp video call.\n\n"
                + "Thank you,\nHarsha Multispeciality Hospital";
    }

    private String buildBookingCancelledMessage(Appointment appointment) {

        return "Dear " + appointment.getPatient().getName() + ",\n\n"
                + "Your appointment has been CANCELLED ❌\n\n"
                + "Hospital: Harsha Multispeciality Hospital\n"
                + "Doctor: " + appointment.getDoctor().getName() + "\n"
                + "Date: " + appointment.getSlot().getSlotDate() + "\n"
                + "Time: " + appointment.getSlot().getStartTime() + "\n\n"
                + "If payment was completed, refund will be processed as per hospital policy.\n\n"
                + "Thank you,\nHarsha Multispeciality Hospital";
    }

    private String buildBookingRescheduledMessage(Appointment appointment) {

        return "Dear " + appointment.getPatient().getName() + ",\n\n"
                + "Your appointment has been RESCHEDULED 🔁\n\n"
                + "Hospital: Harsha Multispeciality Hospital\n"
                + "Doctor: " + appointment.getDoctor().getName() + "\n"
                + "New Date: " + appointment.getSlot().getSlotDate() + "\n"
                + "New Time: " + appointment.getSlot().getStartTime() + "\n\n"
                + "Consultation will happen via WhatsApp video call.\n\n"
                + "Thank you,\nHarsha Multispeciality Hospital";
    }

    // =========================================================
    // SAFE SEND (IDEMPOTENT)
    // =========================================================
    private void safeSend(Appointment appointment,
                          Patient patient,
                          NotificationType type,
                          NotificationChannel channel,
                          String destination,
                          String subject,
                          String message) {

        // destination missing -> skip silently
        if (destination == null || destination.isBlank()) {
            return;
        }

        // prevent duplicate notifications
        boolean alreadySent = notificationLogRepository
                .existsByAppointmentIdAndTypeAndChannel(appointment.getId(), type, channel);

        if (alreadySent) {
            return;
        }

        NotificationLog log = new NotificationLog();
        log.setAppointment(appointment);
        log.setPatient(patient);
        log.setType(type);
        log.setChannel(channel);
        log.setDestination(destination);
        log.setMessage(message);
        log.setStatus(NotificationStatus.PENDING);

        log = notificationLogRepository.save(log);

        try {
            if (channel == NotificationChannel.EMAIL) {
                sendEmail(destination, subject, message);
            } else {
                sendWhatsAppSandbox(destination, message);
            }

            log.setStatus(NotificationStatus.SENT);
            log.setSentAt(LocalDateTime.now());
            notificationLogRepository.save(log);

        } catch (Exception e) {

            log.setStatus(NotificationStatus.FAILED);
            log.setError(e.getMessage());
            notificationLogRepository.save(log);

            System.out.println("⚠️ Notification failed: " + e.getMessage());
        }
    }

    private void sendEmail(String to, String subject, String body) {

        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(to);
        mail.setSubject(subject);
        mail.setText(body);

        mailSender.send(mail);
    }

    private void sendWhatsAppSandbox(String phone, String body) {

        System.out.println("📲 WHATSAPP SANDBOX MESSAGE TO: " + phone);
        System.out.println(body);
    }
}
