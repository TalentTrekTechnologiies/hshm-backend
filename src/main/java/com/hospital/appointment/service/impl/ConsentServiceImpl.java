package com.hospital.appointment.service.impl;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hospital.appointment.entity.Patient;
import com.hospital.appointment.entity.PatientConsent;
import com.hospital.appointment.exception.ResourceNotFoundException;
import com.hospital.appointment.repository.PatientConsentRepository;
import com.hospital.appointment.repository.PatientRepository;
import com.hospital.appointment.service.ConsentService;

@Service
public class ConsentServiceImpl implements ConsentService {

    private final PatientConsentRepository consentRepository;
    private final PatientRepository patientRepository;

    @Value("${app.consent.version:v1}")
    private String consentVersion;

    @Value("${app.consent.text:Telemedicine Consent}")
    private String consentText;

    public ConsentServiceImpl(PatientConsentRepository consentRepository,
                              PatientRepository patientRepository) {
        this.consentRepository = consentRepository;
        this.patientRepository = patientRepository;
    }

    @Override
    @Transactional
    public PatientConsent acceptConsent(Long patientId) {

        // idempotent: if already accepted return existing
        return consentRepository.findByPatientIdAndVersion(patientId, consentVersion)
                .orElseGet(() -> {

                    Patient patient = patientRepository.findById(patientId)
                            .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

                    PatientConsent consent = new PatientConsent();
                    consent.setPatient(patient);
                    consent.setVersion(consentVersion);
                    consent.setConsentText(consentText);
                    consent.setAcceptedAt(LocalDateTime.now());

                    return consentRepository.save(consent);
                });
    }

    @Override
    public boolean hasAcceptedConsent(Long patientId) {
        return consentRepository.existsByPatientIdAndVersion(patientId, consentVersion);
    }
}
