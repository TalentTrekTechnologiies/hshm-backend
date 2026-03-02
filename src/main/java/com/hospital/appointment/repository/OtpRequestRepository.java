package com.hospital.appointment.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.hospital.appointment.entity.OtpRequest;
import com.hospital.appointment.enums.OtpStatus;

public interface OtpRequestRepository extends JpaRepository<OtpRequest, Long> {

    Optional<OtpRequest> findTopByEmailOrderByCreatedAtDesc(String email);

    Optional<OtpRequest> findTopByEmailAndStatusOrderByCreatedAtDesc(String email, OtpStatus status);
}
