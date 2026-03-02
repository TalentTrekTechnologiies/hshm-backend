package com.hospital.appointment.service;

import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.stereotype.Service;

import com.hospital.appointment.entity.OtpRequest;
import com.hospital.appointment.enums.OtpStatus;
import com.hospital.appointment.repository.OtpRequestRepository;

@Service
public class OtpService {

    private final OtpRequestRepository otpRequestRepository;
    private final EmailService emailService;

    public OtpService(OtpRequestRepository otpRequestRepository,
                      EmailService emailService) {
        this.otpRequestRepository = otpRequestRepository;
        this.emailService = emailService;
    }

    // =========================================================
    // GENERATE OTP (EMAIL)
    // =========================================================
    public OtpRequest generateOtp(String email) {

        String safeEmail = email.trim().toLowerCase();

        // Resend cooldown: block OTP spam (30 seconds)
        otpRequestRepository.findTopByEmailOrderByCreatedAtDesc(safeEmail)
                .ifPresent(latest -> {
                    if (latest.getCreatedAt() != null &&
                            latest.getCreatedAt().isAfter(LocalDateTime.now().minusSeconds(30))) {
                        throw new RuntimeException("Please wait 30 seconds before requesting OTP again");
                    }
                });

        // Expire any previous SENT OTPs for this email (safety)
        otpRequestRepository.findTopByEmailAndStatusOrderByCreatedAtDesc(safeEmail, OtpStatus.SENT)
                .ifPresent(existing -> {
                    existing.setStatus(OtpStatus.EXPIRED);
                    otpRequestRepository.save(existing);
                });

        String otp = String.valueOf(100000 + new Random().nextInt(900000));

        OtpRequest otpRequest = new OtpRequest();
        otpRequest.setEmail(safeEmail);
        otpRequest.setOtp(otp);
        otpRequest.setStatus(OtpStatus.SENT);
        otpRequest.setAttemptCount(0);
        otpRequest.setExpiresAt(LocalDateTime.now().plusMinutes(5));

        otpRequest = otpRequestRepository.save(otpRequest);

        // ✅ Send OTP via Email (REAL DEMO)
        emailService.sendOtpEmail(safeEmail, otp);

        return otpRequest;
    }

    // =========================================================
    // VERIFY OTP (EMAIL)
    // =========================================================
    public boolean verifyOtp(String email, String otp) {

        String safeEmail = email.trim().toLowerCase();

        OtpRequest latest = otpRequestRepository
                .findTopByEmailAndStatusOrderByCreatedAtDesc(safeEmail, OtpStatus.SENT)
                .orElse(null);

        if (latest == null) {
            return false;
        }

        // Expired check
        if (LocalDateTime.now().isAfter(latest.getExpiresAt())) {
            latest.setStatus(OtpStatus.EXPIRED);
            otpRequestRepository.save(latest);
            return false;
        }

        // Attempt limit
        if (latest.getAttemptCount() >= 5) {
            latest.setStatus(OtpStatus.EXPIRED);
            otpRequestRepository.save(latest);
            return false;
        }

        // Wrong OTP
        if (!latest.getOtp().equals(otp)) {
            latest.setAttemptCount(latest.getAttemptCount() + 1);
            otpRequestRepository.save(latest);
            return false;
        }

        // Success
        latest.setStatus(OtpStatus.VERIFIED);
        otpRequestRepository.save(latest);
        return true;
    }
}

