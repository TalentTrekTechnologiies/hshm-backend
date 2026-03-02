package com.hospital.appointment.service;

import java.time.*;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hospital.appointment.entity.Doctor;
import com.hospital.appointment.entity.DoctorAvailability;
import com.hospital.appointment.entity.Slot;
import com.hospital.appointment.enums.SlotStatus;
import com.hospital.appointment.repository.DoctorAvailabilityRepository;
import com.hospital.appointment.repository.DoctorRepository;
import com.hospital.appointment.repository.SlotRepository;

@Service
public class SlotGenerationService {

    private final DoctorRepository doctorRepository;
    private final DoctorAvailabilityRepository doctorAvailabilityRepository;
    private final SlotRepository slotRepository;

    public SlotGenerationService(
            DoctorRepository doctorRepository,
            DoctorAvailabilityRepository doctorAvailabilityRepository,
            SlotRepository slotRepository) {
        this.doctorRepository = doctorRepository;
        this.doctorAvailabilityRepository = doctorAvailabilityRepository;
        this.slotRepository = slotRepository;
    }

    @Transactional
    public int generateSlotsForDoctor(Long doctorId, int daysAhead) {

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        List<DoctorAvailability> availabilityList = doctorAvailabilityRepository.findByDoctorId(doctorId);

        if (availabilityList.isEmpty()) {
            throw new RuntimeException("No availability configured for this doctor");
        }

        int createdCount = 0;
        LocalDate today = LocalDate.now();

        for (int i = 0; i <= daysAhead; i++) {
            LocalDate date = today.plusDays(i);
            DayOfWeek dayOfWeek = date.getDayOfWeek();

            for (DoctorAvailability availability : availabilityList) {

                if (availability.getDayOfWeek() == null) continue;

                // Match day
                if (!availability.getDayOfWeek().equalsIgnoreCase(dayOfWeek.name())) {
                    continue;
                }

                LocalTime start = availability.getStartTime();
                LocalTime end = availability.getEndTime();
                int duration = availability.getSlotDuration();

                if (start == null || end == null || duration <= 0) {
                    continue;
                }

                LocalTime current = start;

                while (current.plusMinutes(duration).isBefore(end) || current.plusMinutes(duration).equals(end)) {

                    LocalTime slotEnd = current.plusMinutes(duration);

                    // Avoid duplicates
                    boolean exists = slotRepository.existsByDoctorIdAndSlotDateAndStartTime(doctorId, date, current);

                    if (!exists) {
                        Slot slot = new Slot();
                        slot.setDoctor(doctor);
                        slot.setSlotDate(date);
                        slot.setStartTime(current);
                        slot.setEndTime(slotEnd);
                        slot.setStatus(SlotStatus.AVAILABLE);

                        slotRepository.save(slot);
                        createdCount++;
                    }

                    current = slotEnd;
                }
            }
        }

        return createdCount;
    }
}

