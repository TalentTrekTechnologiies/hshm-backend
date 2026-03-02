package com.hospital.appointment.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hospital.appointment.entity.Patient;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    Optional<Patient> findByPhone(String phone);

    Optional<Patient> findByEmail(String email);

    boolean existsByPhone(String phone);

    boolean existsByEmail(String email);

    // ✅ NEW (Login with phone OR email)
    @Query("""
        select p from Patient p
        where p.phone = :phone
           or lower(p.email) = lower(:email)
    """)
    Optional<Patient> findByPhoneOrEmail(@Param("phone") String phone,
                                        @Param("email") String email);
}
