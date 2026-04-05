package com.fip.appointmentapi.controller;

import com.fip.appointmentapi.entity.AuditLog;
import com.fip.appointmentapi.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    public ResponseEntity<List<AuditLog>> getAllLogs() {
        return ResponseEntity.ok(auditLogService.getAllLogs());
    }

    @GetMapping("/cycle/{cycleId}")
    public ResponseEntity<List<AuditLog>> getLogsByCycle(@PathVariable Integer cycleId) {
        return ResponseEntity.ok(auditLogService.getLogsByCycle(cycleId));
    }

    @GetMapping("/action/{action}")
    public ResponseEntity<List<AuditLog>> getLogsByAction(@PathVariable String action) {
        return ResponseEntity.ok(auditLogService.getLogsByAction(action));
    }
}