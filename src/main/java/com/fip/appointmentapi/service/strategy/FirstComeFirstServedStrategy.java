package com.fip.appointmentapi.service.strategy;

import com.fip.appointmentapi.entity.*;
import com.fip.appointmentapi.repository.AllocationRepository;
import com.fip.appointmentapi.repository.RoomRepository;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


@Component("FIRST_COME_FIRST_SERVED")
public class FirstComeFirstServedStrategy extends BaseAllocationStrategy {

    public FirstComeFirstServedStrategy(RoomRepository roomRepository,
                                        AllocationRepository allocationRepository) {
        super(roomRepository, allocationRepository);
    }

    @Override
    public List<Allocation> allocate(List<Student> students, List<Room> rooms, int cycleId) {
        List<Student> ordered = students.stream()
                .sorted(Comparator.comparing(Student::getId))
                .toList();
        return doAllocate(ordered, rooms, cycleId);
    }

    @Override
    public String getStrategyName() { return "FIRST_COME_FIRST_SERVED"; }
}