package com.hospital.appointment.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hospital.appointment.entity.Appointment;
import com.hospital.appointment.entity.Patient;
import com.hospital.appointment.entity.Slot;
import com.hospital.appointment.enums.AppointmentStatus;
import com.hospital.appointment.exception.ResourceNotFoundException;
import com.hospital.appointment.exception.SlotLockException;
import com.hospital.appointment.repository.AppointmentRepository;
import com.hospital.appointment.repository.PatientRepository;
import com.hospital.appointment.repository.SlotRepository;

@Service
public class SlotLockService {

    private final SlotRepository slotRepository;
    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;

    @Value("${app.slot.lock-minutes:5}")
    private int lockMinutes;

    public SlotLockService(SlotRepository slotRepository,
                           AppointmentRepository appointmentRepository,
                           PatientRepository patientRepository) {
        this.slotRepository = slotRepository;
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
    }

    @Transactional
    public Appointment lockSlotAndCreateAppointment(Long slotId, Long patientId) {

        LocalDateTime lockUntil = LocalDateTime.now().plusMinutes(lockMinutes);

        // Try locking slot
        int updated = slotRepository.lockSlot(slotId, patientId, lockUntil);

        if (updated == 0) {
            throw new SlotLockException("Slot already locked or booked. Please choose another slot.");
        }

        // Fetch slot
        Slot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Slot not found"));

        // Fetch patient
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        // Create appointment
        Appointment appointment = new Appointment();
        appointment.setSlot(slot);
        appointment.setDoctor(slot.getDoctor());
        appointment.setPatient(patient);
        appointment.setStatus(AppointmentStatus.PENDING_PAYMENT);

        appointmentRepository.save(appointment);

        return appointment;
    }
}
