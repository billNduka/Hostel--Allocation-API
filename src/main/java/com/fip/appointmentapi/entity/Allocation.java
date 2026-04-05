package com.fip.appointmentapi.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "allocations")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class Allocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = true)  // nullable for waitlisted students
    private Room room;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AllocationStatus status;

    @Column(name = "allocation_date", nullable = false)
    private LocalDateTime allocationDate;

    @Column(name = "allocation_cycle_id", nullable = false)
    private int allocationCycleId;

    @Column(name = "waitlist_position")
    private Integer waitlistPosition;  // null if ALLOCATED, 1-N if WAITLISTED

    public Allocation(Student student, Room room, AllocationStatus status, int allocationCycleId) {
        this.student = student;
        this.room = room;
        this.status = status;
        this.allocationCycleId = allocationCycleId;
        this.allocationDate = LocalDateTime.now();
    }
}