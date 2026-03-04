package com.hospital.appointment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hospital.appointment.entity.Appointment;
import com.hospital.appointment.enums.AppointmentStatus;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // =========================================================
    // PATIENT APPOINTMENT HISTORY (Fix LazyInitializationException)
    // =========================================================
    @Query("""
        SELECT a
        FROM Appointment a
        JOIN FETCH a.doctor
        JOIN FETCH a.slot
        JOIN FETCH a.patient
        WHERE a.patient.id = :patientId
        ORDER BY a.createdAt DESC
    """)
    List<Appointment> findByPatientIdWithDetails(@Param("patientId") Long patientId);

    // =========================================================
    // DOCTOR APPOINTMENT HISTORY
    // =========================================================
    @Query("""
        SELECT a
        FROM Appointment a
        JOIN FETCH a.patient
        JOIN FETCH a.slot
        WHERE a.doctor.id = :doctorId
        ORDER BY a.createdAt DESC
    """)
    List<Appointment> findByDoctorIdWithDetails(@Param("doctorId") Long doctorId);

    // =========================================================
    // ADMIN VIEW BY STATUS
    // =========================================================
    List<Appointment> findByStatus(AppointmentStatus status);

    // =========================================================
    // PREVENT DOUBLE BOOKING
    // =========================================================
    boolean existsBySlotId(Long slotId);

    // =========================================================
    // IDEMPOTENT CONFIRM (Avoid duplicate appointment creation)
    // =========================================================
    Optional<Appointment> findBySlotId(Long slotId);

    // =========================================================
    // CHECK ACTIVE SLOT BOOKINGS
    // =========================================================
    boolean existsBySlotIdAndStatusIn(Long slotId, List<AppointmentStatus> statuses);
}
