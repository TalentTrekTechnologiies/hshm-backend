package com.hospital.appointment.service;

import com.hospital.appointment.entity.PatientConsent;

public interface ConsentService {

    PatientConsent acceptConsent(Long patientId);

    boolean hasAcceptedConsent(Long patientId);
}
