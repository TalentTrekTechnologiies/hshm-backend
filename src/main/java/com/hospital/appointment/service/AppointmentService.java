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
    // HISTORY & DASHBOARDS
    // =========================================================

    // Patient appointment history (Profile Page)
    List<Appointment> getPatientAppointments(Long patientId);

    // Doctor appointment history (Doctor Dashboard)
    List<Appointment> getDoctorAppointments(Long doctorId);

    // Admin filter by status
    List<Appointment> getAppointmentsByStatus(AppointmentStatus status);
}