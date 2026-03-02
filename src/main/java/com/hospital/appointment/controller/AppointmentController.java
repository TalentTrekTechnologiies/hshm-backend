package com.hospital.appointment.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.hospital.appointment.entity.Appointment;
import com.hospital.appointment.enums.AppointmentStatus;
import com.hospital.appointment.service.AppointmentService;

@RestController
@RequestMapping("/api/appointments")
@CrossOrigin
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    // =========================================================
    // CONFIRM APPOINTMENT (After Payment Verified)
    // =========================================================
    @PostMapping("/confirm")
    public ResponseEntity<?> confirm(@RequestParam Long paymentId) {

        Appointment appointment = appointmentService.confirmAppointment(paymentId);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Appointment confirmed");
        response.put("appointmentId", appointment.getId());
        response.put("status", appointment.getStatus());
        response.put("doctorId", appointment.getDoctor().getId());
        response.put("slotId", appointment.getSlot().getId());
        response.put("patientId", appointment.getPatient().getId());

        return ResponseEntity.ok(response);
    }

    // =========================================================
    // CANCEL APPOINTMENT
    // =========================================================
    @PostMapping("/{appointmentId}/cancel")
    public ResponseEntity<?> cancel(@PathVariable Long appointmentId,
                                    @RequestParam(defaultValue = "User cancelled") String reason) {

        Appointment appointment = appointmentService.cancelAppointment(appointmentId, reason);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Appointment cancelled");
        response.put("appointmentId", appointment.getId());
        response.put("status", appointment.getStatus());
        response.put("slotId", appointment.getSlot().getId());

        return ResponseEntity.ok(response);
    }

    // =========================================================
    // PATIENT APPOINTMENT HISTORY
    // =========================================================
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<?> patientHistory(@PathVariable Long patientId) {

        List<Appointment> list = appointmentService.getPatientAppointments(patientId);
        List<Map<String, Object>> appointments = new ArrayList<>();

        for (Appointment a : list) {

            Map<String, Object> obj = new HashMap<>();
            obj.put("appointmentId", a.getId());
            obj.put("status", a.getStatus());
            obj.put("createdAt", a.getCreatedAt());

            obj.put("doctorId", a.getDoctor().getId());
            obj.put("doctorName", a.getDoctor().getName());

            obj.put("slotId", a.getSlot().getId());
            obj.put("slotDate", a.getSlot().getSlotDate());
            obj.put("startTime", a.getSlot().getStartTime());
            obj.put("endTime", a.getSlot().getEndTime());

            appointments.add(obj);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Patient appointment history");
        response.put("patientId", patientId);
        response.put("count", appointments.size());
        response.put("appointments", appointments);

        return ResponseEntity.ok(response);
    }

    // =========================================================
    // DOCTOR APPOINTMENT HISTORY
    // =========================================================
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<?> doctorHistory(@PathVariable Long doctorId) {

        List<Appointment> list = appointmentService.getDoctorAppointments(doctorId);
        List<Map<String, Object>> appointments = new ArrayList<>();

        for (Appointment a : list) {

            Map<String, Object> obj = new HashMap<>();
            obj.put("appointmentId", a.getId());
            obj.put("status", a.getStatus());
            obj.put("createdAt", a.getCreatedAt());

            obj.put("patientId", a.getPatient().getId());
            obj.put("patientName", a.getPatient().getName());

            obj.put("slotId", a.getSlot().getId());
            obj.put("slotDate", a.getSlot().getSlotDate());
            obj.put("startTime", a.getSlot().getStartTime());
            obj.put("endTime", a.getSlot().getEndTime());

            appointments.add(obj);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Doctor appointment history");
        response.put("doctorId", doctorId);
        response.put("count", appointments.size());
        response.put("appointments", appointments);

        return ResponseEntity.ok(response);
    }

    // =========================================================
    // ADMIN - FILTER BY STATUS
    // =========================================================
    @GetMapping("/status/{status}")
    public ResponseEntity<?> byStatus(@PathVariable AppointmentStatus status) {

        List<Appointment> list = appointmentService.getAppointmentsByStatus(status);
        List<Map<String, Object>> appointments = new ArrayList<>();

        for (Appointment a : list) {

            Map<String, Object> obj = new HashMap<>();
            obj.put("appointmentId", a.getId());
            obj.put("status", a.getStatus());
            obj.put("createdAt", a.getCreatedAt());

            obj.put("patientId", a.getPatient().getId());
            obj.put("doctorId", a.getDoctor().getId());

            obj.put("slotId", a.getSlot().getId());
            obj.put("slotDate", a.getSlot().getSlotDate());
            obj.put("startTime", a.getSlot().getStartTime());
            obj.put("endTime", a.getSlot().getEndTime());

            appointments.add(obj);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Appointments by status");
        response.put("status", status);
        response.put("count", appointments.size());
        response.put("appointments", appointments);

        return ResponseEntity.ok(response);
    }
}