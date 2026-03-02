package com.hospital.appointment.service;

import com.hospital.appointment.entity.Appointment;

public interface NotificationService {

    void sendBookingConfirmed(Appointment appointment);

    void sendBookingCancelled(Appointment appointment);

    void sendBookingRescheduled(Appointment appointment);
}
