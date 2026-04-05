package com.fip.appointmentapi.service.strategy;

import com.fip.appointmentapi.entity.Allocation;
import com.fip.appointmentapi.entity.Room;
import com.fip.appointmentapi.entity.Student;
import java.util.List;

public interface AllocationStrategy
{
    List<Allocation> allocate(List<Student> students, List<Room> rooms, int cycleId);
    String getStrategyName();
}