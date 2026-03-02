package com.hospital.appointment.repository;


import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hospital.appointment.entity.NotificationLog;
import com.hospital.appointment.enums.NotificationChannel;
import com.hospital.appointment.enums.NotificationType;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {

    Optional<NotificationLog> findByAppointmentIdAndTypeAndChannel(Long appointmentId,
                                                                   NotificationType type,
                                                                   NotificationChannel channel);

    boolean existsByAppointmentIdAndTypeAndChannel(Long appointmentId,
                                                   NotificationType type,
                                                   NotificationChannel channel);
}
