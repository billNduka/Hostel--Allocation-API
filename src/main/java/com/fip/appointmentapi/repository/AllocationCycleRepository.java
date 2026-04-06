package com.fip.appointmentapi.repository;

import com.fip.appointmentapi.entity.AllocationCycle;
import com.fip.appointmentapi.entity.CycleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AllocationCycleRepository extends JpaRepository<AllocationCycle, Integer> {
    List<AllocationCycle> findByStatus(CycleStatus status);
}