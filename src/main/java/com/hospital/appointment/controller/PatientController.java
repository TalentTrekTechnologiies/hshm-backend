package com.hospital.appointment.controller;

import java.time.LocalDate;
import java.util.*;

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
    // GET PROFILE
    // =========================================================
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Authentication authentication) {

        Long patientId = extractPatientId(authentication);

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new BadRequestException("Patient not found"));

        Map<String, Object> response = new HashMap<>();

        response.put("patientId", patient.getId());
        response.put("patientCode", patient.getPatientCode());
        response.put("name", patient.getName());
        response.put("email", patient.getEmail());
        response.put("phone", patient.getPhone());
        response.put("gender", patient.getGender());
        response.put("age", patient.getAge());
        response.put("dob", patient.getDateOfBirth());
        response.put("bloodGroup", patient.getBloodGroup());
        response.put("address", patient.getAddress());
        response.put("city", patient.getCity());
        response.put("state", patient.getState());
        response.put("pincode", patient.getPincode());
        response.put("emergencyContact", patient.getEmergencyContact());
        response.put("createdAt", patient.getCreatedAt());
        response.put("active", patient.isActive());

        return ResponseEntity.ok(response);
    }

    // =========================================================
    // UPDATE PROFILE
    // =========================================================
    @PutMapping("/update")
    public ResponseEntity<?> updateProfile(Authentication authentication,
                                           @RequestBody Map<String, String> request) {

        Long patientId = extractPatientId(authentication);

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new BadRequestException("Patient not found"));

        if (request.get("name") != null)
            patient.setName(request.get("name"));

        if (request.get("phone") != null) {
            String newPhone = request.get("phone");
            if (!newPhone.equals(patient.getPhone()) &&
                patientRepository.existsByPhone(newPhone)) {
                throw new BadRequestException("Phone already registered");
            }
            patient.setPhone(newPhone);
        }

        if (request.get("email") != null)
            patient.setEmail(request.get("email"));

        if (request.get("gender") != null)
            patient.setGender(request.get("gender"));

        if (request.get("age") != null)
            patient.setAge(Integer.valueOf(request.get("age")));

        if (request.get("dob") != null)
            patient.setDateOfBirth(LocalDate.parse(request.get("dob")));

        if (request.get("bloodGroup") != null)
            patient.setBloodGroup(request.get("bloodGroup"));

        if (request.get("address") != null)
            patient.setAddress(request.get("address"));

        if (request.get("city") != null)
            patient.setCity(request.get("city"));

        if (request.get("state") != null)
            patient.setState(request.get("state"));

        if (request.get("pincode") != null)
            patient.setPincode(request.get("pincode"));

        if (request.get("emergencyContact") != null)
            patient.setEmergencyContact(request.get("emergencyContact"));

        patientRepository.save(patient);

        return ResponseEntity.ok(Map.of(
                "message", "Profile updated successfully"
        ));
    }

    // =========================================================
    // GET FAMILY MEMBERS
    // =========================================================
    @GetMapping("/family")
    public ResponseEntity<?> getFamilyMembers(Authentication authentication) {

        Long parentId = extractPatientId(authentication);

        List<Patient> familyMembers = patientRepository.findByParentId(parentId);

        List<Map<String, Object>> response = new ArrayList<>();

        for (Patient p : familyMembers) {
            response.add(Map.of(
                    "id", p.getId(),
                    "name", p.getName(),
                    "phone", p.getPhone(),
                    "gender", p.getGender(),
                    "age", p.getAge()
            ));
        }

        return ResponseEntity.ok(response);
    }

    // =========================================================
    // ADD FAMILY MEMBER
    // =========================================================
    @PostMapping("/family")
    public ResponseEntity<?> addFamilyMember(Authentication authentication,
                                             @RequestBody Map<String, String> request) {

        Long parentId = extractPatientId(authentication);

        Patient parent = patientRepository.findById(parentId)
                .orElseThrow(() -> new BadRequestException("Parent not found"));

        Patient member = new Patient();
        member.setName(request.get("name"));
        member.setPhone(request.get("phone"));
        member.setGender(request.get("gender"));
        member.setAge(Integer.valueOf(request.get("age")));
        member.setParent(parent);
        member.setActive(true);
        member.setOtpVerified(true);
        member.setPatientCode("FAM-" + System.currentTimeMillis());

        patientRepository.save(member);

        return ResponseEntity.ok(Map.of(
                "message", "Family member added successfully"
        ));
    }

    // =========================================================
    // UPDATE FAMILY MEMBER
    // =========================================================
    @PutMapping("/family/{id}")
    public ResponseEntity<?> updateFamilyMember(Authentication authentication,
                                                @PathVariable Long id,
                                                @RequestBody Map<String, String> request) {

        Long parentId = extractPatientId(authentication);

        Patient member = patientRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Family member not found"));

        if (member.getParent() == null ||
                !member.getParent().getId().equals(parentId)) {
            throw new BadRequestException("Unauthorized access");
        }

        if (request.get("name") != null)
            member.setName(request.get("name"));

        if (request.get("phone") != null)
            member.setPhone(request.get("phone"));

        if (request.get("gender") != null)
            member.setGender(request.get("gender"));

        if (request.get("age") != null)
            member.setAge(Integer.valueOf(request.get("age")));

        patientRepository.save(member);

        return ResponseEntity.ok(Map.of(
                "message", "Family member updated successfully"
        ));
    }

    // =========================================================
    // DELETE FAMILY MEMBER
    // =========================================================
    @DeleteMapping("/family/{id}")
    public ResponseEntity<?> deleteFamilyMember(Authentication authentication,
                                                @PathVariable Long id) {

        Long parentId = extractPatientId(authentication);

        Patient member = patientRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Family member not found"));

        if (member.getParent() == null ||
                !member.getParent().getId().equals(parentId)) {
            throw new BadRequestException("Unauthorized access");
        }

        patientRepository.delete(member);

        return ResponseEntity.ok(Map.of(
                "message", "Family member deleted successfully"
        ));
    }

    // =========================================================
    // HELPER METHOD
    // =========================================================
    private Long extractPatientId(Authentication authentication) {

        if (authentication == null || authentication.getName() == null) {
            throw new BadRequestException("Unauthorized access");
        }

        return Long.valueOf(authentication.getName());
    }
}