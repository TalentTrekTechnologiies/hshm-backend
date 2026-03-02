package com.hospital.appointment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hospital.appointment.entity.Appointment;
import com.hospital.appointment.enums.AppointmentStatus;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // ✅ Patient Appointment History
    List<Appointment> findByPatientIdOrderByCreatedAtDesc(Long patientId);

    // ✅ Doctor Appointment History
    List<Appointment> findByDoctorIdOrderByCreatedAtDesc(Long doctorId);

    // ✅ Admin view - all confirmed appointments
    List<Appointment> findByStatus(AppointmentStatus status);

    // ✅ Prevent duplicate appointment for same slot
    boolean existsBySlotId(Long slotId);

    // ✅ Needed for idempotent confirm (avoid duplicate confirm)
    Optional<Appointment> findBySlotId(Long slotId);

    boolean existsBySlotIdAndStatusIn(Long slotId, List<AppointmentStatus> statuses);
}

