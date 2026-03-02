package com.hospital.appointment.repository;

import com.hospital.appointment.entity.PaymentConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentConfigRepository extends JpaRepository<PaymentConfig, Long> {
}