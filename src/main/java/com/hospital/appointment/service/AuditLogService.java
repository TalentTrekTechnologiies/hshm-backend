package com.hospital.appointment.service;

public interface AuditLogService {

    void log(String actionType,
             String entityType,
             Long entityId,
             String performedBy,
             String ipAddress,
             String description);
}
