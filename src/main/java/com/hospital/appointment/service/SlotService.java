package com.hospital.appointment.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hospital.appointment.entity.Doctor;
import com.hospital.appointment.entity.Slot;
import com.hospital.appointment.enums.SlotStatus;
import com.hospital.appointment.exception.BadRequestException;
import com.hospital.appointment.exception.ResourceNotFoundException;
import com.hospital.appointment.repository.DoctorRepository;
import com.hospital.appointment.repository.SlotRepository;

@Service
public class SlotService {

    private final SlotRepository slotRepository;
    private final DoctorRepository doctorRepository;

    public SlotService(SlotRepository slotRepository,
                       DoctorRepository doctorRepository) {
        this.slotRepository = slotRepository;
        this.doctorRepository = doctorRepository;
    }

    // =========================================================
    // PATIENT LISTING APIs
    // =========================================================

    public List<Slot> getSlotsForDoctor(Long doctorId, String date) {
        LocalDate slotDate = LocalDate.parse(date);
        return slotRepository.findByDoctorIdAndSlotDateOrderByStartTimeAsc(doctorId, slotDate);
    }

    public List<Slot> getAvailableSlotsForDoctor(Long doctorId, String date) {
        LocalDate slotDate = LocalDate.parse(date);
        return slotRepository.findByDoctorIdAndSlotDateAndStatusOrderByStartTimeAsc(
                doctorId,
                slotDate,
                SlotStatus.AVAILABLE
        );
    }

    public Slot getSlotById(Long slotId) {
        return slotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Slot not found"));
    }

    // =========================================================
    // ADMIN APIs
    // =========================================================

    @Transactional
    public Map<String, Object> generateSlots(Long doctorId,
                                            String date,
                                            String start,
                                            String end) {

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        if (!doctor.isActive()) {
            throw new BadRequestException("Doctor is inactive. Cannot generate slots.");
        }

        Integer durationObj = doctor.getSlotDurationMinutes();

        if (durationObj == null || durationObj <= 0) {
            throw new BadRequestException("Doctor slotDurationMinutes must be greater than 0");
        }

        int durationMinutes = durationObj;

        LocalDate slotDate = LocalDate.parse(date);
        LocalTime startTime = LocalTime.parse(start);
        LocalTime endTime = LocalTime.parse(end);

        if (!startTime.isBefore(endTime)) {
            throw new BadRequestException("Start time must be before end time");
        }

        int createdCount = 0;
        int skippedCount = 0;

        LocalTime cursor = startTime;

        while (cursor.plusMinutes(durationMinutes).compareTo(endTime) <= 0) {

            LocalTime slotStart = cursor;
            LocalTime slotEnd = cursor.plusMinutes(durationMinutes);

            boolean exists = slotRepository.existsByDoctorIdAndSlotDateAndStartTime(
                    doctorId,
                    slotDate,
                    slotStart
            );

            if (exists) {
                skippedCount++;
            } else {
                Slot slot = new Slot();
                slot.setDoctor(doctor);
                slot.setSlotDate(slotDate);
                slot.setStartTime(slotStart);
                slot.setEndTime(slotEnd);
                slot.setStatus(SlotStatus.AVAILABLE);

                slotRepository.save(slot);
                createdCount++;
            }

            cursor = cursor.plusMinutes(durationMinutes);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Slot generation completed");
        response.put("doctorId", doctorId);
        response.put("date", slotDate.toString());
        response.put("durationMinutes", durationMinutes);
        response.put("createdSlots", createdCount);
        response.put("skippedSlots", skippedCount);

        return response;
    }

    @Transactional
    public Map<String, Object> blockSlot(Long slotId) {

        Slot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Slot not found"));

        if (slot.getStatus() == SlotStatus.BOOKED) {
            throw new BadRequestException("Booked slot cannot be blocked");
        }

        slot.setStatus(SlotStatus.BLOCKED);
        slot.setLockedBy(null);
        slot.setLockUntil(null);

        slotRepository.save(slot);

        return Map.of(
                "message", "Slot blocked successfully",
                "slotId", slotId,
                "status", slot.getStatus()
        );
    }

    @Transactional
    public Map<String, Object> unblockSlot(Long slotId) {

        Slot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Slot not found"));

        if (slot.getStatus() != SlotStatus.BLOCKED) {
            throw new BadRequestException("Only BLOCKED slots can be unblocked");
        }

        slot.setStatus(SlotStatus.AVAILABLE);
        slot.setLockedBy(null);
        slot.setLockUntil(null);

        slotRepository.save(slot);

        return Map.of(
                "message", "Slot unblocked successfully",
                "slotId", slotId,
                "status", slot.getStatus()
        );
    }
}
