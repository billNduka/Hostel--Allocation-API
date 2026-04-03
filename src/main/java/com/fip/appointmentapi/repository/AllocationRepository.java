package com.fip.appointmentapi.repository;

import com.fip.appointmentapi.entity.Allocation;
import com.fip.appointmentapi.entity.AllocationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AllocationRepository extends JpaRepository<Allocation, Long>
{
    List<Allocation> findById(int cycleId);
    List<Allocation> findByStatus(AllocationStatus status);
    Optional<Allocation> findByStudentIdAndStatus(Long studentId, AllocationStatus status);
    int countByRoomId(Long roomId);

    List<Allocation> findByRoomId(Long roomId);

    // check if student already has an active allocation before assigning
    boolean existsByStudentIdAndStatus(Long studentId, AllocationStatus status);
}