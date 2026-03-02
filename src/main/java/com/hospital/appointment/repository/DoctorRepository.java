package com.hospital.appointment.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hospital.appointment.entity.Doctor;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    List<Doctor> findByDepartmentIdAndActiveTrue(Long departmentId);

    List<Doctor> findByActiveTrue();

    // prevent duplicate doctor name in same department
    boolean existsByNameIgnoreCaseAndDepartmentId(String name, Long departmentId);
}
