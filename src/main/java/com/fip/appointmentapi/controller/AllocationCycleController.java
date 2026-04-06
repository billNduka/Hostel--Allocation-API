package com.fip.appointmentapi.controller;

import com.fip.appointmentapi.entity.AllocationCycle;
import com.fip.appointmentapi.entity.CycleStatus;
import com.fip.appointmentapi.repository.AllocationCycleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/cycles")
@RequiredArgsConstructor
public class AllocationCycleController {

    private final AllocationCycleRepository cycleRepository;

    @GetMapping
    public ResponseEntity<List<AllocationCycle>> getAllCycles() {
        return ResponseEntity.ok(cycleRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AllocationCycle> getCycleById(@PathVariable int id) {
        return ResponseEntity.ok(
                cycleRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Cycle not found: " + id))
        );
    }

    @GetMapping("/completed")
    public ResponseEntity<List<AllocationCycle>> getCompletedCycles() {
        return ResponseEntity.ok(cycleRepository.findByStatus(CycleStatus.COMPLETED));
    }
}