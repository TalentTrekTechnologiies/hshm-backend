package com.hospital.appointment.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hospital.appointment.entity.Department;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    List<Department> findByActiveTrue();

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
