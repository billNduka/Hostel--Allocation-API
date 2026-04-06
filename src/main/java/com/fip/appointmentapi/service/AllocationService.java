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
            throw new RuntimeException("Unknown strategy: " + activeStrategy);
        }

        List<Student> students = studentRepository.findAll();
        List<Room> rooms = roomRepository.findAvailableRooms();

        auditLogService.log(
                "CYCLE_STARTED",
                "Cycle " + cycleId + " started using: " + activeStrategy
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
                "Cycle " + cycleId + " completed | Allocated: "
                        + allocated + " | Waitlisted: " + waitlisted,
                cycleId
        );

        return results;
    }

    @Transactional
    public Allocation reallocateStudent(Long studentId, Long newRoomId) {
        int cycleId = getNextCycleId();

        // find current active allocation
        Allocation current = allocationRepository
                .findByStudentIdAndStatus(studentId, AllocationStatus.ALLOCATED)
                .orElseThrow(() -> new RuntimeException(
                        "No active allocation found for student: " + studentId));

        Room oldRoom = current.getRoom();
        Room newRoom = roomRepository.findById(newRoomId)
                .orElseThrow(() -> new RuntimeException("Room not found: " + newRoomId));

        // validate gender match
        if (newRoom.getGender() != current.getStudent().getGender()) {
            throw new RuntimeException(
                    "Gender mismatch: student is "
                            + current.getStudent().getGender()
                            + " but room is " + newRoom.getGender()
            );
        }

        // validate room has space
        if (newRoom.isFull()) {
            throw new RuntimeException(
                    "Room " + newRoom.getRoomNumber() + " is at full capacity"
            );
        }

        // mark old allocation as reallocated
        current.setStatus(AllocationStatus.REALLOCATED);
        allocationRepository.save(current);

        // free up old room
        oldRoom.setOccupied(oldRoom.getOccupied() - 1);
        roomRepository.save(oldRoom);

        // create new allocation
        Allocation newAllocation = new Allocation(
                current.getStudent(), newRoom, AllocationStatus.ALLOCATED, cycleId
        );
        allocationRepository.save(newAllocation);

        // fill old room's vacancy from waitlist if anyone is waiting
        newRoom.setOccupied(newRoom.getOccupied() + 1);
        roomRepository.save(newRoom);

        promoteFromWaitlist(oldRoom, cycleId);

        auditLogService.log(
                "REALLOCATED",
                "Student " + current.getStudent().getMatricNumber()
                        + " moved from room " + oldRoom.getRoomNumber()
                        + " to room " + newRoom.getRoomNumber(),
                cycleId
        );

        return newAllocation;
    }

    @Transactional
    public void promoteFromWaitlist(Room room, int cycleId) {
        if (room.isFull()) return;

        // get the next person on the waitlist by position
        allocationRepository
                .findTopByStatusOrderByWaitlistPositionAsc(AllocationStatus.WAITLISTED)
                .ifPresent(waitlisted -> {

                    // gender check before promoting
                    if (waitlisted.getStudent().getGender() != room.getGender()) return;

                    waitlisted.setStatus(AllocationStatus.ALLOCATED);
                    waitlisted.setRoom(room);
                    waitlisted.setWaitlistPosition(null);
                    allocationRepository.save(waitlisted);

                    room.setOccupied(room.getOccupied() + 1);
                    roomRepository.save(room);

                    auditLogService.log(
                            "WAITLIST_PROMOTED",
                            "Student " + waitlisted.getStudent().getMatricNumber()
                                    + " promoted from waitlist to room " + room.getRoomNumber(),
                            cycleId
                    );
                });
    }

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