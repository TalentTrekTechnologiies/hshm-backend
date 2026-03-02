package com.hospital.appointment.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.hospital.appointment.entity.Department;
import com.hospital.appointment.entity.Doctor;
import com.hospital.appointment.exception.BadRequestException;
import com.hospital.appointment.exception.ResourceNotFoundException;
import com.hospital.appointment.repository.DepartmentRepository;
import com.hospital.appointment.repository.DoctorRepository;

@RestController
@RequestMapping("/api/doctors")
@CrossOrigin
public class DoctorController {

    private final DoctorRepository doctorRepository;
    private final DepartmentRepository departmentRepository;

    public DoctorController(DoctorRepository doctorRepository,
                            DepartmentRepository departmentRepository) {
        this.doctorRepository = doctorRepository;
        this.departmentRepository = departmentRepository;
    }

    @GetMapping
    public List<Doctor> getDoctorsByDepartment(@RequestParam Long departmentId) {
        return doctorRepository.findByDepartmentIdAndActiveTrue(departmentId);
    }

    @GetMapping("/active")
    public List<Doctor> getAllActiveDoctors() {
        return doctorRepository.findByActiveTrue();
    }

    // =========================================================
    // CREATE DOCTOR (Admin)
    // =========================================================
    @PostMapping
    public Doctor addDoctor(@RequestBody Doctor doctor) {

        if (doctor.getName() == null || doctor.getName().isBlank()) {
            throw new BadRequestException("Doctor name is required");
        }

        if (doctor.getDepartment() == null || doctor.getDepartment().getId() == null) {
            throw new BadRequestException("Department is required for doctor");
        }

        Department department = departmentRepository.findById(doctor.getDepartment().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));

        if (!department.isActive()) {
            throw new BadRequestException("Cannot add doctor to inactive department");
        }

        String doctorName = doctor.getName().trim();

        // ✅ Prevent duplicates in same department
        boolean exists = doctorRepository.existsByNameIgnoreCaseAndDepartmentId(
                doctorName,
                department.getId()
        );

        if (exists) {
            throw new BadRequestException("Doctor already exists in this department");
        }

        doctor.setName(doctorName);
        doctor.setDepartment(department);
        doctor.setActive(true);

        // slot duration default (DB-configurable)
        if (doctor.getSlotDurationMinutes() == null || doctor.getSlotDurationMinutes() <= 0) {
            doctor.setSlotDurationMinutes(15);
        }

        return doctorRepository.save(doctor);
    }

    // =========================================================
    // UPDATE DOCTOR (Admin)
    // =========================================================
    @PutMapping("/{id}")
    public Doctor updateDoctor(@PathVariable Long id,
                               @RequestBody Doctor req) {

        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        // name update + duplicate check
        if (req.getName() != null && !req.getName().isBlank()) {

            String newName = req.getName().trim();

            // If department exists, check duplicates in same department
            Long deptId = doctor.getDepartment() != null ? doctor.getDepartment().getId() : null;

            if (deptId != null) {
                boolean exists = doctorRepository.existsByNameIgnoreCaseAndDepartmentId(newName, deptId);

                // if same name, allow only if it's same doctor
                if (exists && !doctor.getName().equalsIgnoreCase(newName)) {
                    throw new BadRequestException("Doctor already exists in this department");
                }
            }

            doctor.setName(newName);
        }

        if (req.getQualification() != null) {
            doctor.setQualification(req.getQualification().trim());
        }

        if (req.getSpecialization() != null) {
            doctor.setSpecialization(req.getSpecialization().trim());
        }

        if (req.getExperience() > 0) {
            doctor.setExperience(req.getExperience());
        }

        // slot duration update (DB-configurable)
        if (req.getSlotDurationMinutes() != null && req.getSlotDurationMinutes() > 0) {
            doctor.setSlotDurationMinutes(req.getSlotDurationMinutes());
        }

        // department update (optional)
        if (req.getDepartment() != null && req.getDepartment().getId() != null) {

            Department department = departmentRepository.findById(req.getDepartment().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found"));

            if (!department.isActive()) {
                throw new BadRequestException("Cannot assign doctor to inactive department");
            }

            // ✅ If changing department, check duplicate in new department
            String currentName = doctor.getName().trim();

            boolean exists = doctorRepository.existsByNameIgnoreCaseAndDepartmentId(
                    currentName,
                    department.getId()
            );

            // If exists and department actually changed -> reject
            if (exists && !doctor.getDepartment().getId().equals(department.getId())) {
                throw new BadRequestException("Doctor already exists in this department");
            }

            doctor.setDepartment(department);
        }

        return doctorRepository.save(doctor);
    }

    // =========================================================
    // DISABLE DOCTOR (soft delete)
    // =========================================================
    @PatchMapping("/{id}/disable")
    public ResponseEntity<?> disableDoctor(@PathVariable Long id) {

        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        if (!doctor.isActive()) {
            return ResponseEntity.ok(Map.of(
                    "message", "Doctor already disabled",
                    "doctorId", id
            ));
        }

        doctor.setActive(false);
        doctorRepository.save(doctor);

        return ResponseEntity.ok(Map.of(
                "message", "Doctor disabled successfully",
                "doctorId", id
        ));
    }
}
