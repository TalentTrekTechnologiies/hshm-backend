package com.hospital.appointment.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.hospital.appointment.entity.Department;
import com.hospital.appointment.exception.BadRequestException;
import com.hospital.appointment.exception.ResourceNotFoundException;
import com.hospital.appointment.repository.DepartmentRepository;

@RestController
@RequestMapping("/api/departments")
@CrossOrigin
public class DepartmentController {

    private final DepartmentRepository departmentRepository;

    public DepartmentController(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    // =========================================================
    // 1) LIST ACTIVE DEPARTMENTS (Patient + Admin)
    // =========================================================
    @GetMapping
    public List<Department> getAllActiveDepartments() {
        return departmentRepository.findByActiveTrue();
    }

    // =========================================================
    // 2) CREATE DEPARTMENT (Admin)
    // =========================================================
    @PostMapping
    public Department addDepartment(@RequestBody Department department) {

        if (department.getName() == null || department.getName().isBlank()) {
            throw new BadRequestException("Department name is required");
        }

        boolean exists = departmentRepository.existsByNameIgnoreCase(department.getName().trim());
        if (exists) {
            throw new BadRequestException("Department already exists with same name");
        }

        department.setName(department.getName().trim());
        department.setActive(true);

        return departmentRepository.save(department);
    }

    // =========================================================
    // 3) UPDATE DEPARTMENT (Admin)
    // =========================================================
    @PutMapping("/{id}")
    public Department updateDepartment(@PathVariable Long id,
                                       @RequestBody Department req) {

        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));

        if (req.getName() != null && !req.getName().isBlank()) {
            String newName = req.getName().trim();

            boolean exists = departmentRepository.existsByNameIgnoreCaseAndIdNot(newName, id);
            if (exists) {
                throw new BadRequestException("Another department already exists with same name");
            }

            department.setName(newName);
        }

        if (req.getDescription() != null) {
            department.setDescription(req.getDescription().trim());
        }

        return departmentRepository.save(department);
    }

    // =========================================================
    // 4) DISABLE DEPARTMENT (Admin) - soft delete
    // =========================================================
    @PatchMapping("/{id}/disable")
    public ResponseEntity<?> disableDepartment(@PathVariable Long id) {

        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));

        if (!department.isActive()) {
            return ResponseEntity.ok(Map.of(
                    "message", "Department already disabled",
                    "departmentId", id
            ));
        }

        department.setActive(false);
        departmentRepository.save(department);

        return ResponseEntity.ok(Map.of(
                "message", "Department disabled successfully",
                "departmentId", id
        ));
    }
}

