package com.hospital.appointment.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hospital.appointment.entity.Enquiry;
import com.hospital.appointment.enums.EnquiryStatus;
import com.hospital.appointment.exception.BadRequestException;
import com.hospital.appointment.exception.ResourceNotFoundException;
import com.hospital.appointment.repository.EnquiryRepository;
import com.hospital.appointment.service.EnquiryService;

@Service
public class EnquiryServiceImpl implements EnquiryService {

    private final EnquiryRepository enquiryRepository;

    public EnquiryServiceImpl(EnquiryRepository enquiryRepository) {
        this.enquiryRepository = enquiryRepository;
    }

    // =========================================================
    // 1) PATIENT: CREATE ENQUIRY
    // =========================================================
    @Override
    @Transactional
    public Enquiry createEnquiry(Enquiry enquiry) {

        if (enquiry == null) {
            throw new BadRequestException("Enquiry data is required");
        }

        // name
        if (enquiry.getName() == null || enquiry.getName().trim().isEmpty()) {
            throw new BadRequestException("Name is required");
        }

        // phone
        if (enquiry.getPhone() == null || enquiry.getPhone().trim().isEmpty()) {
            throw new BadRequestException("Phone is required");
        }

        // message
        if (enquiry.getMessage() == null || enquiry.getMessage().trim().isEmpty()) {
            throw new BadRequestException("Message is required");
        }

        // Clean fields
        enquiry.setName(enquiry.getName().trim());
        enquiry.setPhone(enquiry.getPhone().trim());

        // Email optional
        if (enquiry.getEmail() != null && !enquiry.getEmail().trim().isEmpty()) {
            enquiry.setEmail(enquiry.getEmail().trim().toLowerCase());
        } else {
            enquiry.setEmail(null);
        }

        // Department optional
        if (enquiry.getDepartment() != null && !enquiry.getDepartment().trim().isEmpty()) {
            enquiry.setDepartment(enquiry.getDepartment().trim());
        } else {
            enquiry.setDepartment(null);
        }

        // Preferred callback time optional
        if (enquiry.getPreferredCallbackTime() != null && !enquiry.getPreferredCallbackTime().trim().isEmpty()) {
            enquiry.setPreferredCallbackTime(enquiry.getPreferredCallbackTime().trim());
        } else {
            enquiry.setPreferredCallbackTime(null);
        }

        // Message trim
        enquiry.setMessage(enquiry.getMessage().trim());

        // =========================================================
        // SPAM PREVENTION (same phone within 5 minutes)
        // =========================================================
        boolean spam = enquiryRepository.existsByPhoneAndCreatedAtAfter(
                enquiry.getPhone(),
                LocalDateTime.now().minusMinutes(5)
        );

        if (spam) {
            throw new BadRequestException("Please wait before submitting another enquiry");
        }

        // Status
        enquiry.setStatus(EnquiryStatus.NEW);

        // createdAt handled by @PrePersist in entity (recommended)
        // But if you didn't add PrePersist, keep this:
        if (enquiry.getCreatedAt() == null) {
            enquiry.setCreatedAt(LocalDateTime.now());
        }

        return enquiryRepository.save(enquiry);
    }

    // =========================================================
    // 2) ADMIN: GET ALL ENQUIRIES
    // =========================================================
    @Override
    public List<Enquiry> getAllEnquiries() {
        return enquiryRepository.findAllByOrderByCreatedAtDesc();
    }

    // =========================================================
    // 3) ADMIN: GET ENQUIRIES BY STATUS
    // =========================================================
    @Override
    public List<Enquiry> getEnquiriesByStatus(EnquiryStatus status) {

        if (status == null) {
            throw new BadRequestException("Status is required");
        }

        return enquiryRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    // =========================================================
    // 4) ADMIN: UPDATE STATUS
    // =========================================================
    @Override
    @Transactional
    public Enquiry updateEnquiryStatus(Long enquiryId, EnquiryStatus status) {

        if (enquiryId == null) {
            throw new BadRequestException("Enquiry ID is required");
        }

        if (status == null) {
            throw new BadRequestException("Status is required");
        }

        Enquiry enquiry = enquiryRepository.findById(enquiryId)
                .orElseThrow(() -> new ResourceNotFoundException("Enquiry not found"));

        enquiry.setStatus(status);

        return enquiryRepository.save(enquiry);
    }

    // =========================================================
    // 5) ADMIN: GET SINGLE ENQUIRY
    // =========================================================
    @Override
    public Enquiry getEnquiryById(Long enquiryId) {

        if (enquiryId == null) {
            throw new BadRequestException("Enquiry ID is required");
        }

        return enquiryRepository.findById(enquiryId)
                .orElseThrow(() -> new ResourceNotFoundException("Enquiry not found"));
    }
}
