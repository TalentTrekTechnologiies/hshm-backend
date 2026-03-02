package com.hospital.appointment.mapper;

import com.hospital.appointment.DTO.AppointmentResponseDTO;
import com.hospital.appointment.entity.Appointment;

public class AppointmentMapper {

    public static AppointmentResponseDTO toDTO(Appointment a) {

        AppointmentResponseDTO dto = new AppointmentResponseDTO();

        dto.setAppointmentId(a.getId());

        dto.setPatientId(a.getPatient().getId());
        dto.setPatientName(a.getPatient().getName());
        dto.setPatientPhone(a.getPatient().getPhone());

        dto.setDoctorId(a.getDoctor().getId());
        dto.setDoctorName(a.getDoctor().getName());
        dto.setSpecialization(a.getDoctor().getSpecialization());

        dto.setSlotId(a.getSlot().getId());
        dto.setSlotDate(a.getSlot().getSlotDate());
        dto.setStartTime(a.getSlot().getStartTime());
        dto.setEndTime(a.getSlot().getEndTime());

        dto.setStatus(a.getStatus());
       
        dto.setCreatedAt(a.getCreatedAt());

        return dto;
    }
}
