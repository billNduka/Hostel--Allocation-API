package com.fip.appointmentapi.service.strategy;

import com.fip.appointmentapi.entity.*;
import com.fip.appointmentapi.repository.AllocationRepository;
import com.fip.appointmentapi.repository.RoomRepository;
import org.springframework.stereotype.Component;
import java.util.*;

@Component("GREEDY")
public class GreedyStrategy extends BaseAllocationStrategy {

    public GreedyStrategy(RoomRepository roomRepository,
                          AllocationRepository allocationRepository) {
        super(roomRepository, allocationRepository);
    }

    @Override
    public List<Allocation> allocate(List<Student> students, List<Room> rooms, int cycleId) {
        List<Allocation> results = new ArrayList<>();
        int waitlistPosition = 1;

        // sort rooms by capacity descending — fill largest rooms first
        List<Room> sortedRooms = rooms.stream()
                .sorted(Comparator.comparingInt(Room::getCapacity).reversed())
                .toList();

        // group students by gender for clean iteration
        List<Student> unallocated = new ArrayList<>(students);

        for (Room room : sortedRooms) {
            if (unallocated.isEmpty()) break;

            // fill this room completely before moving to the next
            List<Student> eligible = unallocated.stream()
                    .filter(s -> !allocationRepository.existsByStudentIdAndStatus(
                            s.getId(), AllocationStatus.ALLOCATED))
                    .filter(s -> s.getGender() == room.getGender())
                    .toList();

            for (Student student : eligible) {
                if (room.isFull()) break;

                room.setOccupied(room.getOccupied() + 1);
                roomRepository.save(room);

                Allocation allocation = new Allocation(
                        student, room, AllocationStatus.ALLOCATED, cycleId
                );
                results.add(allocationRepository.save(allocation));
                unallocated.remove(student);
            }
        }

        // anyone remaining goes to waitlist
        for (Student student : unallocated) {
            boolean alreadyAllocated = allocationRepository
                    .existsByStudentIdAndStatus(student.getId(), AllocationStatus.ALLOCATED);
            if (alreadyAllocated) continue;

            Allocation waitlisted = new Allocation(
                    student, null, AllocationStatus.WAITLISTED, cycleId
            );
            waitlisted.setWaitlistPosition(waitlistPosition++);
            results.add(allocationRepository.save(waitlisted));
        }

        return results;
    }

    @Override
    public String getStrategyName() {
        return "GREEDY";
    }
}