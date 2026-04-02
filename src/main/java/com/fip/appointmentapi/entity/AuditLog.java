package com.fip.appointmentapi.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class AuditLog
{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String action;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(name = "allocation_cycle_id")
    private Integer allocationCycleId;

    public AuditLog(String action, String details, Integer allocationCycleId)
    {
        this.action = action;
        this.details = details;
        this.allocationCycleId = allocationCycleId;
        this.timestamp = LocalDateTime.now();
    }
}