package com.hospital.appointment.controller;

import com.hospital.appointment.entity.DoctorAvailability;
import com.hospital.appointment.repository.DoctorAvailabilityRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/availability")
@CrossOrigin
public class DoctorAvailabilityController {

    private final DoctorAvailabilityRepository availabilityRepository;

    public DoctorAvailabilityController(DoctorAvailabilityRepository availabilityRepository) {
        this.availabilityRepository = availabilityRepository;
    }

    @GetMapping
    public List<DoctorAvailability> getAvailability(
            @RequestParam Long doctorId
    ) {
        return availabilityRepository.findByDoctorId(doctorId);
    }
    @PostMapping
    public DoctorAvailability addAvailability(
            @RequestBody DoctorAvailability availability) {
        return availabilityRepository.save(availability);
    }

}

