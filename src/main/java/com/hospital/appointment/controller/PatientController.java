package com.hospital.appointment.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.hospital.appointment.entity.Patient;
import com.hospital.appointment.exception.BadRequestException;
import com.hospital.appointment.repository.PatientRepository;

@RestController
@RequestMapping("/api/patient")
public class PatientController {

    private final PatientRepository patientRepository;

    public PatientController(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    // =========================================================
    // GET PROFILE (SECURED - JWT REQUIRED)
    // =========================================================
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Authentication authentication) {

        if (authentication == null || authentication.getName() == null) {
            throw new BadRequestException("Unauthorized access");
        }

        Long patientId = Long.valueOf(authentication.getName());

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new BadRequestException("Patient not found"));

        return ResponseEntity.ok(Map.of(
                "patientId", patient.getId(),
                "patientCode", patient.getPatientCode(),
                "name", patient.getName(),
                "email", patient.getEmail(),
                "phone", patient.getPhone(),
                "gender", patient.getGender(),
                "age", patient.getAge(),
                "createdAt", patient.getCreatedAt(),
                "active", patient.isActive()
        ));
    }

    // =========================================================
    // UPDATE PROFILE (SECURED - JWT REQUIRED)
    // =========================================================
    @PutMapping("/update")
    public ResponseEntity<?> updateProfile(Authentication authentication,
                                           @RequestBody Map<String, String> request) {

        Long patientId = Long.valueOf(authentication.getName());

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new BadRequestException("Patient not found"));

        String name = request.get("name");
        String phone = request.get("phone");

        if (name != null && !name.trim().isEmpty()) {
            patient.setName(name.trim());
        }

        if (phone != null && !phone.trim().isEmpty()) {

            if (!phone.equals(patient.getPhone()) &&
                patientRepository.existsByPhone(phone)) {
                throw new BadRequestException("Phone already registered");
            }

            patient.setPhone(phone.trim());
        }

        patientRepository.save(patient);

        return ResponseEntity.ok(Map.of(
                "message", "Profile updated successfully"
        ));
    }
}