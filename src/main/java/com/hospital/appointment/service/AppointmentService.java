package com.hospital.appointment.service;

import java.util.List;

import com.hospital.appointment.entity.Appointment;
import com.hospital.appointment.enums.AppointmentStatus;

public interface AppointmentService {

    // =========================================================
    // CORE PAYMENT FLOW
    // =========================================================

    // Confirm appointment after verified payment
    Appointment confirmAppointment(Long paymentId);

    // Cancel appointment (may trigger refund)
    Appointment cancelAppointment(Long appointmentId, String reason);

    // =========================================================
    // HISTORY & ADMIN
    // =========================================================

    List<Appointment> getPatientAppointments(Long patientId);

    List<Appointment> getDoctorAppointments(Long doctorId);

    List<Appointment> getAppointmentsByStatus(AppointmentStatus status);
}