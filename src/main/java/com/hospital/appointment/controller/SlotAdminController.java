package com.hospital.appointment.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.hospital.appointment.service.SlotGenerationService;

@RestController
@RequestMapping("/api/admin/slots")
public class SlotAdminController {

    private final SlotGenerationService slotGenerationService;

    public SlotAdminController(SlotGenerationService slotGenerationService) {
        this.slotGenerationService = slotGenerationService;
    }

    @PostMapping("/generate/{doctorId}")
    public ResponseEntity<?> generate(@PathVariable Long doctorId,
                                      @RequestParam(defaultValue = "7") int daysAhead) {

        int created = slotGenerationService.generateSlotsForDoctor(doctorId, daysAhead);

        return ResponseEntity.ok(Map.of(
                "message", "Slots generated successfully",
                "doctorId", doctorId,
                "daysAhead", daysAhead,
                "createdSlots", created
        ));
    }
}

