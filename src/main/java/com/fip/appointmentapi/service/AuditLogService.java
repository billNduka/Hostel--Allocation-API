package com.fip.appointmentapi.service;

import com.fip.appointmentapi.entity.AuditLog;
import com.fip.appointmentapi.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLog log(String action, String details, Integer allocationCycleId) {
        AuditLog entry = new AuditLog(action, details, allocationCycleId);
        return auditLogRepository.save(entry);
    }

    public List<AuditLog> getAllLogs() {
        return auditLogRepository.findAll();
    }

    public List<AuditLog> getLogsByCycle(Integer cycleId) {
        return auditLogRepository.findByAllocationCycleId(cycleId);
    }

    public List<AuditLog> getLogsByAction(String action) {
        return auditLogRepository.findByAction(action);
    }
}