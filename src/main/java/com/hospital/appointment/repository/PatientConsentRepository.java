package com.hospital.appointment.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hospital.appointment.entity.PatientConsent;

public interface PatientConsentRepository extends JpaRepository<PatientConsent, Long> {

    Optional<PatientConsent> findByPatientIdAndVersion(Long patientId, String version);

    boolean existsByPatientIdAndVersion(Long patientId, String version);
}
