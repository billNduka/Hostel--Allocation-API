package com.fip.appointmentapi.service.strategy;

import com.fip.appointmentapi.entity.*;
import com.fip.appointmentapi.repository.AllocationRepository;
import com.fip.appointmentapi.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public abstract class BaseAllocationStrategy implements AllocationStrategy
{

    protected final RoomRepository roomRepository;
    protected final AllocationRepository allocationRepository;

    protected List<Allocation> doAllocate(List<Student> students, List<Room> rooms, int cycleId)
    {
        List<Allocation> results = new ArrayList<>();
        List<Room> availableRooms = new ArrayList<>(rooms);
        int waitlistPosition = 1;

        for (Student student : students) {

            // skip already allocated students
            boolean alreadyAllocated = allocationRepository
                    .existsByStudentIdAndStatus(student.getId(), AllocationStatus.ALLOCATED);
            if (alreadyAllocated) continue;

            // find first room matching gender with space
            Room match = availableRooms.stream()
                    .filter(r -> r.getGender() == student.getGender())
                    .filter(r -> !r.isFull())
                    .findFirst()
                    .orElse(null);

            if (match != null)
            {
                match.setOccupied(match.getOccupied() + 1);
                roomRepository.save(match);

                Allocation allocation = new Allocation(
                        student, match, AllocationStatus.ALLOCATED, cycleId
                );
                results.add(allocationRepository.save(allocation));

                // remove from available list if now full
                if (match.isFull())
                {
                    availableRooms.remove(match);
                }

            } else {
                Allocation waitlisted = new Allocation(
                        student, null, AllocationStatus.WAITLISTED, cycleId
                );
                waitlisted.setWaitlistPosition(waitlistPosition++);
                results.add(allocationRepository.save(waitlisted));
            }
        }

        return results;
    }
}
