package com.fip.appointmentapi.service.strategy;

import com.fip.appointmentapi.entity.*;
import com.fip.appointmentapi.repository.AllocationRepository;
import com.fip.appointmentapi.repository.RoomRepository;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component("RANDOMIZED")
public class RandomizedStrategy extends BaseAllocationStrategy {

    public RandomizedStrategy(RoomRepository roomRepository,
                              AllocationRepository allocationRepository) {
        super(roomRepository, allocationRepository);
    }

    @Override
    public List<Allocation> allocate(List<Student> students, List<Room> rooms, int cycleId) {
        List<Student> shuffled = new ArrayList<>(students);
        Collections.shuffle(shuffled);
        return doAllocate(shuffled, rooms, cycleId);
    }

    @Override
    public String getStrategyName() { return "RANDOMIZED"; }
}