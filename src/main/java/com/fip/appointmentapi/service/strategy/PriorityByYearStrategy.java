package com.fip.appointmentapi.service.strategy;

import com.fip.appointmentapi.entity.*;
import com.fip.appointmentapi.repository.AllocationRepository;
import com.fip.appointmentapi.repository.RoomRepository;
import org.springframework.stereotype.Component;
import java.util.Comparator;
import java.util.List;

@Component("PRIORITY_BY_YEAR")
public class PriorityByYearStrategy extends BaseAllocationStrategy {

    public PriorityByYearStrategy(RoomRepository roomRepository,
                                  AllocationRepository allocationRepository) {
        super(roomRepository, allocationRepository);
    }

    @Override
    public List<Allocation> allocate(List<Student> students, List<Room> rooms, int cycleId) {
        List<Student> ordered = students.stream()
                .sorted(Comparator.comparingInt(Student::getYearOfStudy).reversed())
                .toList();
        return doAllocate(ordered, rooms, cycleId);
    }

    @Override
    public String getStrategyName() { return "PRIORITY_BY_YEAR"; }
}