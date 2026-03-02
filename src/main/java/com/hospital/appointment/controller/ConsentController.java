package com.hospital.appointment.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.hospital.appointment.entity.PatientConsent;
import com.hospital.appointment.service.ConsentService;

@RestController
@RequestMapping("/api/consent")
@CrossOrigin
public class ConsentController {

    private final ConsentService consentService;

    public ConsentController(ConsentService consentService) {
        this.consentService = consentService;
    }

    @PostMapping("/accept")
    public ResponseEntity<?> accept(@RequestParam Long patientId) {

        PatientConsent consent = consentService.acceptConsent(patientId);

        return ResponseEntity.ok(Map.of(
                "message", "Consent accepted",
                "patientId", patientId,
                "version", consent.getVersion(),
                "acceptedAt", consent.getAcceptedAt()
        ));
    }

    @GetMapping("/status")
    public ResponseEntity<?> status(@RequestParam Long patientId) {

        boolean accepted = consentService.hasAcceptedConsent(patientId);

        return ResponseEntity.ok(Map.of(
                "patientId", patientId,
                "consentAccepted", accepted
        ));
    }
}

