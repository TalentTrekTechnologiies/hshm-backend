package com.hospital.appointment.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.hospital.appointment.entity.Patient;
import com.hospital.appointment.exception.BadRequestException;
import com.hospital.appointment.repository.PatientRepository;
import com.hospital.appointment.config.JwtService;
import com.hospital.appointment.service.OtpService;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final OtpService otpService;
    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(OtpService otpService,
                          PatientRepository patientRepository,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService) {
        this.otpService = otpService;
        this.patientRepository = patientRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    // =========================================================
    // REGISTER: REQUEST OTP (ONLY IF EMAIL PROVIDED)
    // =========================================================
    @PostMapping("/register/request-otp")
    public ResponseEntity<?> registerRequestOtp(@RequestBody Map<String, Object> request) {

        String name = (String) request.get("name");
        String phone = (String) request.get("phone");
        String email = (String) request.get("email");
        String gender = (String) request.get("gender");
        Integer age = request.get("age") == null ? null : Integer.valueOf(request.get("age").toString());

        if (name == null || phone == null || gender == null || age == null) {
            throw new BadRequestException("Name, phone, gender and age are required");
        }

        String safePhone = phone.trim();

        if (patientRepository.existsByPhone(safePhone)) {
            throw new BadRequestException("User already registered with this phone.");
        }

        String safeEmail = null;

        if (email != null && !email.isBlank()) {

            safeEmail = email.trim().toLowerCase();

            if (patientRepository.existsByEmail(safeEmail)) {
                throw new BadRequestException("User already registered with this email.");
            }

            // Send OTP ONLY if email exists
            otpService.generateOtp(safeEmail);
        }

        return ResponseEntity.ok(Map.of(
                "message", safeEmail != null ? "OTP sent for registration" : "Email not provided. OTP skipped."
        ));
    }

    // =========================================================
    // REGISTER: VERIFY OTP OR DIRECT REGISTER
    // =========================================================
    @PostMapping("/register/verify-otp")
    public ResponseEntity<?> registerVerifyOtp(@RequestBody Map<String, Object> request) {

        String name = (String) request.get("name");
        String phone = (String) request.get("phone");
        String email = (String) request.get("email");
        String gender = (String) request.get("gender");
        Integer age = Integer.valueOf(request.get("age").toString());
        String otp = (String) request.get("otp");
        String password = (String) request.get("password");
        String confirmPassword = (String) request.get("confirmPassword");

        if (!password.equals(confirmPassword)) {
            throw new BadRequestException("Passwords do not match");
        }

        String safeEmail = null;

        // OTP verification only if email exists
        if (email != null && !email.isBlank()) {

            safeEmail = email.trim().toLowerCase();

            boolean verified = otpService.verifyOtp(safeEmail, otp);

            if (!verified) {
                throw new BadRequestException("Invalid or expired OTP");
            }
        }

        Patient patient = new Patient();

        patient.setName(name);
        patient.setPhone(phone);
        patient.setEmail(safeEmail);
        patient.setGender(gender.toUpperCase());
        patient.setAge(age);
        patient.setOtpVerified(true);
        patient.setPasswordHash(passwordEncoder.encode(password));

        patient.setPatientCode("TEMP");

        patient = patientRepository.save(patient);

        patient.setPatientCode("PAT-" + String.format("%06d", patient.getId()));

        patientRepository.save(patient);

        return ResponseEntity.ok(Map.of(
                "message", "Registration successful",
                "patientId", patient.getId(),
                "patientCode", patient.getPatientCode()
        ));
    }

    // =========================================================
    // LOGIN: REQUEST OTP
    // =========================================================
    @PostMapping("/login/request-otp")
    public ResponseEntity<?> loginRequestOtp(@RequestBody Map<String, String> request) {

        String email = request.get("email");
        String safeEmail = email.trim().toLowerCase();

        Patient patient = patientRepository.findByEmail(safeEmail)
                .orElseThrow(() -> new BadRequestException("User not registered"));

        otpService.generateOtp(safeEmail);

        return ResponseEntity.ok(Map.of(
                "message", "OTP sent for login",
                "email", safeEmail
        ));
    }

    // =========================================================
    // LOGIN: VERIFY OTP
    // =========================================================
    @PostMapping("/login/verify-otp")
    public ResponseEntity<?> loginVerifyOtp(@RequestBody Map<String, String> request) {

        String email = request.get("email");
        String otp = request.get("otp");
        String safeEmail = email.trim().toLowerCase();

        Patient patient = patientRepository.findByEmail(safeEmail)
                .orElseThrow(() -> new BadRequestException("User not registered"));

        boolean verified = otpService.verifyOtp(safeEmail, otp);

        if (!verified) {
            throw new BadRequestException("Invalid or expired OTP");
        }

        String token = jwtService.generateToken(patient.getId().toString());

        return ResponseEntity.ok(Map.of(
                "message", "Login successful (OTP)",
                "token", token,
                "patientId", patient.getId(),
                "patientCode", patient.getPatientCode()
        ));
    }

    // =========================================================
    // LOGIN WITH PASSWORD
    // =========================================================
    @PostMapping("/login/password")
    public ResponseEntity<?> loginWithPassword(@RequestBody Map<String, String> request) {

        String phoneOrEmail = request.get("phoneOrEmail");
        String password = request.get("password");

        Patient patient = patientRepository.findByPhoneOrEmail(phoneOrEmail, phoneOrEmail.toLowerCase())
                .orElseThrow(() -> new BadRequestException("Patient not registered"));

        if (!passwordEncoder.matches(password, patient.getPasswordHash())) {
            throw new BadRequestException("Invalid password");
        }

        String token = jwtService.generateToken(patient.getId().toString());

        return ResponseEntity.ok(Map.of(
                "message", "Login successful (PASSWORD)",
                "token", token,
                "patientId", patient.getId(),
                "patientCode", patient.getPatientCode()
        ));
    }

    // =========================================================
    // FORGOT PASSWORD: REQUEST OTP
    // =========================================================
    @PostMapping("/forgot-password/request-otp")
    public ResponseEntity<?> forgotPasswordRequestOtp(@RequestBody Map<String, String> request) {

        String email = request.get("email");
        String safeEmail = email.trim().toLowerCase();

        patientRepository.findByEmail(safeEmail)
                .orElseThrow(() -> new BadRequestException("User not registered"));

        otpService.generateOtp(safeEmail);

        return ResponseEntity.ok(Map.of(
                "message", "OTP sent for password reset",
                "email", safeEmail
        ));
    }

    // =========================================================
    // FORGOT PASSWORD: VERIFY OTP
    // =========================================================
    @PostMapping("/forgot-password/verify-otp")
    public ResponseEntity<?> forgotPasswordVerifyOtp(@RequestBody Map<String, String> request) {

        String email = request.get("email");
        String otp = request.get("otp");
        String newPassword = request.get("newPassword");

        String safeEmail = email.trim().toLowerCase();

        Patient patient = patientRepository.findByEmail(safeEmail)
                .orElseThrow(() -> new BadRequestException("User not registered"));

        boolean verified = otpService.verifyOtp(safeEmail, otp);

        if (!verified) {
            throw new BadRequestException("Invalid or expired OTP");
        }

        patient.setPasswordHash(passwordEncoder.encode(newPassword));

        patientRepository.save(patient);

        return ResponseEntity.ok(Map.of(
                "message", "Password reset successful"
        ));
    }
}