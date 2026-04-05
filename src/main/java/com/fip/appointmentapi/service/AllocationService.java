package com.fip.appointmentapi.service;

import com.fip.appointmentapi.entity.*;
import com.fip.appointmentapi.repository.*;
import com.fip.appointmentapi.service.strategy.AllocationStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AllocationService {

    private final AllocationRepository allocationRepository;
    private final StudentRepository studentRepository;
    private final RoomRepository roomRepository;
    private final AuditLogService auditLogService;

    // Spring injects all AllocationStrategy beans into this map
    // key = the @Component name e.g. "FIRST_COME_FIRST_SERVED"
    private final Map<String, AllocationStrategy> strategies;

    @Value("${allocation.strategy:FIRST_COME_FIRST_SERVED}")
    private String activeStrategy;

    public List<Allocation> getAllAllocations() {
        return allocationRepository.findAll();
    }

    public Allocation getAllocationById(Long id) {
        return allocationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Allocation not found with id: " + id));
    }

    public List<Allocation> getAllocationsByCycle(int cycleId) {
        return allocationRepository.findByAllocationCycleId(cycleId);
    }

    public List<Allocation> getWaitlistedAllocations() {
        return allocationRepository.findByStatus(AllocationStatus.WAITLISTED);
    }

    public Allocation updateAllocationStatus(Long id, AllocationStatus newStatus) {
        Allocation allocation = getAllocationById(id);
        AllocationStatus oldStatus = allocation.getStatus();
        allocation.setStatus(newStatus);
        Allocation saved = allocationRepository.save(allocation);

        auditLogService.log(
                "STATUS_CHANGE",
                "Allocation " + id + " changed from " + oldStatus + " to " + newStatus,
                allocation.getAllocationCycleId()
        );

        return saved;
    }

    @Transactional
    public List<Allocation> runAllocationCycle() {
        int cycleId = getNextCycleId();

        AllocationStrategy strategy = strategies.get(activeStrategy);
        if (strategy == null) {
            throw new RuntimeException("Unknown allocation strategy: " + activeStrategy);
        }

        List<Student> students = studentRepository.findAll();
        List<Room> rooms = roomRepository.findAvailableRooms();

        auditLogService.log(
                "CYCLE_STARTED",
                "Cycle " + cycleId + " started using strategy: " + activeStrategy
                        + " | Students: " + students.size()
                        + " | Available rooms: " + rooms.size(),
                cycleId
        );

        List<Allocation> results = strategy.allocate(students, rooms, cycleId);

        long allocated = results.stream()
                .filter(a -> a.getStatus() == AllocationStatus.ALLOCATED).count();
        long waitlisted = results.stream()
                .filter(a -> a.getStatus() == AllocationStatus.WAITLISTED).count();

        auditLogService.log(
                "CYCLE_COMPLETED",
                "Cycle " + cycleId + " completed | Allocated: " + allocated
                        + " | Waitlisted: " + waitlisted,
                cycleId
        );

        return results;
    }

    // allows switching strategy at runtime without restarting
    public void setActiveStrategy(String strategyName) {
        if (!strategies.containsKey(strategyName)) {
            throw new RuntimeException("Unknown strategy: " + strategyName
                    + ". Available: " + strategies.keySet());
        }
        this.activeStrategy = strategyName;
    }

    public String getActiveStrategy() {
        return activeStrategy;
    }

    private int getNextCycleId() {
        return allocationRepository.findAll()
                .stream()
                .mapToInt(Allocation::getAllocationCycleId)
                .max()
                .orElse(0) + 1;
    }
}