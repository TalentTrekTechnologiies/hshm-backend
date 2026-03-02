package com.hospital.appointment.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hospital.appointment.service.AppointmentService;

@RestController
@RequestMapping("/api/admin/appointments")
@CrossOrigin
public class AdminAppointmentController {

    private final AppointmentService appointmentService;

    public AdminAppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    // ---------------------------------------------------------
    // Admin endpoints can be added here later if required.
    // Currently no reschedule logic exists in production core.
    // ---------------------------------------------------------

}
