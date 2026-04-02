package com.fip.appointmentapi.repository;

import com.fip.appointmentapi.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long>
{
    List<AuditLog> findByAllocationCycleId(Integer cycleId);
    List<AuditLog> findByAction(String action);
    List<AuditLog> findByAllocationCycleIdAndAction(Integer cycleId, String action);
    List<AuditLog> findByAllocationCycleIdOrderByTimestampAsc(Integer cycleId);
    List<AuditLog> findByAllocationCycleIdOrderByTimestampDesc(Integer cycleId);
}