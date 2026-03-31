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
public class Allocation
{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AllocationStatus status;

    @Column(name = "allocation_date", nullable = false)
    private LocalDateTime allocationDate;

    @Column(name = "allocation_run", nullable = false)
    private int allocationRun;

    public Allocation(Student student, Room room, AllocationStatus status, int allocationRun)
    {
        this.student = student;
        this.room = room;
        this.status = status;
        this.allocationRun = allocationRun;
        this.allocationDate = LocalDateTime.now();
    }
}
