package com.fip.appointmentapi.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "allocation_cycles")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class AllocationCycle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String strategy;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    @Column
    private LocalDateTime completedAt;

    @Column(nullable = false)
    private int totalStudents;

    @Column
    private int allocated;

    @Column
    private int waitlisted;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CycleStatus status;

    public AllocationCycle(String strategy, int totalStudents) {
        this.strategy = strategy;
        this.totalStudents = totalStudents;
        this.startedAt = LocalDateTime.now();
        this.status = CycleStatus.IN_PROGRESS;
    }
}