package com.hospital.appointment.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.hospital.appointment.entity.Appointment;
import com.hospital.appointment.service.SlotLockService;
import com.hospital.appointment.service.SlotService;

@RestController
@RequestMapping("/api/slots")
@CrossOrigin
public class SlotController {

    private final SlotLockService slotLockService;
    private final SlotService slotService;

    public SlotController(SlotLockService slotLockService, SlotService slotService) {
        this.slotLockService = slotLockService;
        this.slotService = slotService;
    }

    // =========================================================
    // PATIENT APIs
    // =========================================================

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<?> getAllSlotsForDoctor(@PathVariable Long doctorId,
                                                  @RequestParam String date) {
        return ResponseEntity.ok(slotService.getSlotsForDoctor(doctorId, date));
    }

    @GetMapping("/doctor/{doctorId}/available")
    public ResponseEntity<?> getAvailableSlotsForDoctor(@PathVariable Long doctorId,
                                                        @RequestParam String date) {
        return ResponseEntity.ok(
                slotService.getAvailableSlotsForDoctor(doctorId, date)
        );
    }

    @GetMapping("/{slotId}")
    public ResponseEntity<?> getSlotById(@PathVariable Long slotId) {
        return ResponseEntity.ok(slotService.getSlotById(slotId));
    }

    // =========================================================
    // SLOT LOCK + APPOINTMENT CREATION
    // =========================================================

    @PostMapping("/{slotId}/lock")
    public ResponseEntity<?> lockSlot(@PathVariable Long slotId,
                                      @RequestParam Long patientId) {

        Appointment appointment =
                slotLockService.lockSlotAndCreateAppointment(slotId, patientId);

        return ResponseEntity.ok(Map.of(
                "message", "Slot locked successfully",
                "slotId", slotId,
                "appointmentId", appointment.getId(),
                "patientId", appointment.getPatient().getId(),
                "doctorId", appointment.getDoctor().getId(),
                "status", appointment.getStatus()
        ));
    }

    // =========================================================
    // ADMIN APIs (Phase-1 required)
    // =========================================================

    @PostMapping("/admin/generate")
    public ResponseEntity<?> generateSlots(@RequestParam Long doctorId,
                                           @RequestParam String date,
                                           @RequestParam String start,
                                           @RequestParam String end) {

        return ResponseEntity.ok(
                slotService.generateSlots(doctorId, date, start, end)
        );
    }

    @PatchMapping("/{slotId}/block")
    public ResponseEntity<?> blockSlot(@PathVariable Long slotId) {
        return ResponseEntity.ok(slotService.blockSlot(slotId));
    }

    @PatchMapping("/{slotId}/unblock")
    public ResponseEntity<?> unblockSlot(@PathVariable Long slotId) {
        return ResponseEntity.ok(slotService.unblockSlot(slotId));
    }
}