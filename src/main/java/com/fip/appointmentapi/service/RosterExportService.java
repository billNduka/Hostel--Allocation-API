package com.fip.appointmentapi.service;

import com.fip.appointmentapi.entity.Allocation;
import com.fip.appointmentapi.entity.AllocationStatus;
import com.fip.appointmentapi.repository.AllocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RosterExportService {

    private final AllocationRepository allocationRepository;

    public byte[] exportRosterAsCsv(Integer cycleId) {
        List<Allocation> allocations = cycleId != null
                ? allocationRepository.findByAllocationCycleId(cycleId)
                : allocationRepository.findByStatus(AllocationStatus.ALLOCATED);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(out);

        // header row
        writer.println(
                "student_id,matric_number,student_name,gender,year_of_study," +
                        "hostel_name,room_number,room_type,capacity,status,allocation_date,cycle_id"
        );

        for (Allocation a : allocations) {
            if (a.getRoom() == null) continue; // skip waitlisted — no room assigned

            writer.println(String.join(",",
                    String.valueOf(a.getStudent().getId()),
                    a.getStudent().getMatricNumber(),
                    escapeCsv(a.getStudent().getName()),
                    a.getStudent().getGender().toString(),
                    String.valueOf(a.getStudent().getYearOfStudy()),
                    escapeCsv(a.getRoom().getHostel().getName()),
                    a.getRoom().getRoomNumber(),
                    a.getRoom().getCapacity().toString(),
                    String.valueOf(a.getRoom().getCapacity()),
                    a.getStatus().toString(),
                    a.getAllocationDate().toString(),
                    String.valueOf(a.getAllocationCycleId())
            ));
        }

        writer.flush();
        return out.toByteArray();
    }

    public byte[] exportWaitlistAsCsv() {
        List<Allocation> waitlisted = allocationRepository
                .findByStatus(AllocationStatus.WAITLISTED);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(out);

        writer.println(
                "position,student_id,matric_number,student_name,gender,year_of_study,cycle_id"
        );

        waitlisted.stream()
                .sorted((a, b) -> {
                    int posA = a.getWaitlistPosition() != null ? a.getWaitlistPosition() : Integer.MAX_VALUE;
                    int posB = b.getWaitlistPosition() != null ? b.getWaitlistPosition() : Integer.MAX_VALUE;
                    return Integer.compare(posA, posB);
                })
                .forEach(a -> writer.println(String.join(",",
                        String.valueOf(a.getWaitlistPosition()),
                        String.valueOf(a.getStudent().getId()),
                        a.getStudent().getMatricNumber(),
                        escapeCsv(a.getStudent().getName()),
                        a.getStudent().getGender().toString(),
                        String.valueOf(a.getStudent().getYearOfStudy()),
                        String.valueOf(a.getAllocationCycleId())
                )));

        writer.flush();
        return out.toByteArray();
    }

    // wraps values containing commas in quotes to prevent broken CSV columns
    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}