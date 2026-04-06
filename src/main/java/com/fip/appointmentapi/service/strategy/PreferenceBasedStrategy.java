package com.fip.appointmentapi.service.strategy;

import com.fip.appointmentapi.entity.*;
import com.fip.appointmentapi.repository.AllocationRepository;
import com.fip.appointmentapi.repository.RoomRepository;
import org.springframework.stereotype.Component;
import java.util.*;

@Component("PREFERENCE_BASED")
public class PreferenceBasedStrategy extends BaseAllocationStrategy
{

    public PreferenceBasedStrategy(RoomRepository roomRepository, AllocationRepository allocationRepository)
    {
        super(roomRepository, allocationRepository);
    }

    @Override
    public List<Allocation> allocate(List<Student> students, List<Room> rooms, int cycleId)
    {
        // senior students get their preferences honoured first
        List<Student> ordered = students.stream()
                .sorted(Comparator.comparingInt(Student::getYearOfStudy).reversed())
                .toList();

        List<Allocation> results = new ArrayList<>();
        List<Room> availableRooms = new ArrayList<>(rooms);
        int waitlistPosition = 1;

        for (Student student : ordered)
        {

            boolean alreadyAllocated = allocationRepository.existsByStudentIdAndStatus(student.getId(), AllocationStatus.ALLOCATED);
            if (alreadyAllocated) continue;

            Room match = findRoomForStudent(student, availableRooms);

            if (match != null)
            {
                match.setOccupied(match.getOccupied() + 1);
                roomRepository.save(match);

                Allocation allocation = new Allocation(
                        student, match, AllocationStatus.ALLOCATED, cycleId
                );
                results.add(allocationRepository.save(allocation));

                if (match.isFull()) availableRooms.remove(match);

            } else
            {
                Allocation waitlisted = new Allocation(
                        student, null, AllocationStatus.WAITLISTED, cycleId
                );
                waitlisted.setWaitlistPosition(waitlistPosition++);
                results.add(allocationRepository.save(waitlisted));
            }
        }

        return results;
    }

    private Room findRoomForStudent(Student student, List<Room> availableRooms) {

        // base filter every room must pass regardless of preferences
        List<Room> genderMatched = availableRooms.stream()
                .filter(r -> r.getGender() == student.getGender())
                .filter(r -> !r.isFull())
                .toList();

        if (genderMatched.isEmpty()) return null;

        // student has no preferences — return first available
        if (!student.hasPreferences()) {
            return genderMatched.get(0);
        }

        // try each preference in order
        for (StudentPreference preference : student.getPreferences()) {
            Room match = switch (preference.getType()) {

                case HOSTEL_NAME -> genderMatched.stream()
                        .filter(r -> r.getHostel().getName()
                                .equalsIgnoreCase(preference.getValue()))
                        .findFirst()
                        .orElse(null);

                case ROOM_CAPACITY -> genderMatched.stream()
                        .filter(r -> r.getCapacity() == parseCapacity(preference.getValue()))
                        .findFirst()
                        .orElse(null);
            };

            if (match != null) return match;
        }

        // no preference matched — fall back to first available room
        return genderMatched.get(0);
    }

    private int parseCapacity(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            // if someone stored a bad capacity value, treat it as no match
            return -1;
        }
    }

    @Override
    public String getStrategyName() {
        return "PREFERENCE_BASED";
    }
}