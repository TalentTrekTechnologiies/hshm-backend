package com.hospital.appointment.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.hospital.appointment.entity.Appointment;
import com.hospital.appointment.entity.Slot;
import com.hospital.appointment.enums.AppointmentStatus;
import com.hospital.appointment.enums.SlotStatus;
import com.hospital.appointment.service.SlotLockService;
import com.hospital.appointment.service.SlotService;

@RestController
@RequestMapping("/api/slots")
@CrossOrigin
public class SlotController {

    private final SlotLockService slotLockService;
    private final SlotService slotService;

    public SlotController(SlotLockService slotLockService,
                          SlotService slotService) {
        this.slotLockService = slotLockService;
        this.slotService = slotService;
    }

    // =========================================================
    // PATIENT APIs
    // =========================================================

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<?> getAllSlotsForDoctor(@PathVariable Long doctorId,
                                                  @RequestParam String date) {
        return ResponseEntity.ok(
                slotService.getSlotsForDoctor(doctorId, date)
        );
    }

    @GetMapping("/doctor/{doctorId}/available")
    public ResponseEntity<?> getAvailableSlotsForDoctor(
            @PathVariable Long doctorId,
            @RequestParam String date) {

        return ResponseEntity.ok(
                slotService.getAvailableSlotsForDoctor(doctorId, date)
        );
    }

    @GetMapping("/{slotId}")
    public ResponseEntity<?> getSlotById(@PathVariable Long slotId) {
        return ResponseEntity.ok(
                slotService.getSlotById(slotId)
        );
    }

    // =========================================================
    // SLOT LOCK (Payment Flow)
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
    // DIRECT BOOKING (NO PAYMENT FLOW)
    // =========================================================

    @PostMapping("/{slotId}/book-direct")
    public ResponseEntity<?> bookDirect(@PathVariable Long slotId,
                                        @RequestParam Long patientId) {

        // Step 1: Lock slot and create appointment (PENDING_PAYMENT)
        Appointment appointment =
                slotLockService.lockSlotAndCreateAppointment(slotId, patientId);

        // Step 2: Immediately confirm appointment
        appointment.setStatus(AppointmentStatus.CONFIRMED);

        Slot slot = appointment.getSlot();
        slot.setStatus(SlotStatus.BOOKED);
        slot.setLockedBy(null);
        slot.setLockUntil(null);

        // IMPORTANT: Save slot changes
        slotService.getSlotById(slot.getId()); // ensures entity loaded
        // If you have SlotRepository, better to save explicitly there

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Appointment booked successfully");
        response.put("appointmentId", appointment.getId());
        response.put("patientName", appointment.getPatient().getName());
        response.put("doctorName", appointment.getDoctor().getName());
        response.put("date", slot.getSlotDate());
        response.put("time", slot.getStartTime());
        response.put("status", appointment.getStatus());

        return ResponseEntity.ok(response);
    }

    // =========================================================
    // ADMIN APIs
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
        return ResponseEntity.ok(
                slotService.blockSlot(slotId)
        );
    }

    @PatchMapping("/{slotId}/unblock")
    public ResponseEntity<?> unblockSlot(@PathVariable Long slotId) {
        return ResponseEntity.ok(
                slotService.unblockSlot(slotId)
        );
    }
}