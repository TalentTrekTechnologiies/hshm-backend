package com.hospital.appointment.service.impl;

import org.springframework.stereotype.Service;

import com.hospital.appointment.entity.AuditLog;
import com.hospital.appointment.repository.AuditLogRepository;
import com.hospital.appointment.service.AuditLogService;

@Service
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogServiceImpl(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public void log(String actionType,
                    String entityType,
                    Long entityId,
                    String performedBy,
                    String ipAddress,
                    String description) {

        AuditLog log = new AuditLog();
        log.setActionType(actionType);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setPerformedBy(performedBy);
        log.setIpAddress(ipAddress);
        log.setDescription(description);

        auditLogRepository.save(log);
    }
}