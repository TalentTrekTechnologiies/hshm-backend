package com.hospital.appointment.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.hospital.appointment.entity.Enquiry;
import com.hospital.appointment.enums.EnquiryStatus;
import com.hospital.appointment.service.EnquiryService;

@RestController
@RequestMapping("/api/enquiries")
@CrossOrigin(origins = "*")
public class EnquiryController {

    private final EnquiryService enquiryService;

    public EnquiryController(EnquiryService enquiryService) {
        this.enquiryService = enquiryService;
    }

    // =========================================================
    // PATIENT API
    // =========================================================

    // ✅ Patient submits enquiry
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Enquiry enquiry) {

        Enquiry saved = enquiryService.createEnquiry(enquiry);

        return ResponseEntity.ok(Map.of(
                "message", "Enquiry submitted successfully. Our team will contact you soon.",
                "enquiryId", saved.getId(),
                "status", saved.getStatus()
        ));
    }

    // =========================================================
    // ADMIN APIs
    // =========================================================

    // ✅ Admin: get all enquiries (latest first)
    @GetMapping("/admin/all")
    public ResponseEntity<?> getAll() {

        List<Enquiry> list = enquiryService.getAllEnquiries();

        return ResponseEntity.ok(Map.of(
                "message", "All enquiries",
                "count", list.size(),
                "enquiries", list
        ));
    }

    // ✅ Admin: filter enquiries by status
    @GetMapping("/admin/status/{status}")
    public ResponseEntity<?> getByStatus(@PathVariable EnquiryStatus status) {

        List<Enquiry> list = enquiryService.getEnquiriesByStatus(status);

        return ResponseEntity.ok(Map.of(
                "message", "Enquiries by status",
                "status", status,
                "count", list.size(),
                "enquiries", list
        ));
    }

    // ✅ Admin: get single enquiry
    @GetMapping("/admin/{id}")
    public ResponseEntity<?> getOne(@PathVariable Long id) {

        Enquiry enquiry = enquiryService.getEnquiryById(id);

        return ResponseEntity.ok(Map.of(
                "message", "Enquiry details",
                "enquiry", enquiry
        ));
    }

    // ✅ Admin: update enquiry status
    @PatchMapping("/admin/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id,
                                          @RequestParam EnquiryStatus status) {

        Enquiry updated = enquiryService.updateEnquiryStatus(id, status);

        return ResponseEntity.ok(Map.of(
                "message", "Enquiry status updated",
                "enquiryId", updated.getId(),
                "status", updated.getStatus()
        ));
    }
}
