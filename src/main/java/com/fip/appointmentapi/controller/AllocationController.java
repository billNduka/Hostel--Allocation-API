package com.fip.appointmentapi.controller;

import com.fip.appointmentapi.entity.Allocation;
import com.fip.appointmentapi.entity.AllocationStatus;
import com.fip.appointmentapi.entity.Room;
import com.fip.appointmentapi.repository.RoomRepository;
import com.fip.appointmentapi.service.AllocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/allocations")
@RequiredArgsConstructor
public class AllocationController {

    private final AllocationService allocationService;
    private final RoomRepository roomRepository;

    @PostMapping("/run")
    public ResponseEntity<List<Allocation>> runAllocationCycle() {
        return ResponseEntity.ok(allocationService.runAllocationCycle());
    }

    @GetMapping
    public ResponseEntity<List<Allocation>> getAllAllocations() {
        return ResponseEntity.ok(allocationService.getAllAllocations());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Allocation> getAllocationById(@PathVariable Long id) {
        return ResponseEntity.ok(allocationService.getAllocationById(id));
    }

    @GetMapping("/cycle/{cycleId}")
    public ResponseEntity<List<Allocation>> getAllocationsByCycle(@PathVariable int cycleId) {
        return ResponseEntity.ok(allocationService.getAllocationsByCycle(cycleId));
    }

    @GetMapping("/waitlist")
    public ResponseEntity<List<Allocation>> getWaitlist() {
        return ResponseEntity.ok(allocationService.getWaitlistedAllocations());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Allocation> updateStatus(
            @PathVariable Long id,
            @RequestParam AllocationStatus status) {
        return ResponseEntity.ok(allocationService.updateAllocationStatus(id, status));
    }

    @GetMapping("/strategy")
    public ResponseEntity<String> getActiveStrategy() {
        return ResponseEntity.ok(allocationService.getActiveStrategy());
    }

    @PutMapping("/strategy")
    public ResponseEntity<String> setActiveStrategy(@RequestParam String strategy) {
        allocationService.setActiveStrategy(strategy);
        return ResponseEntity.ok("Strategy changed to: " + strategy);
    }

    @PostMapping("/{studentId}/reallocate")
    public ResponseEntity<Allocation> reallocateStudent(
            @PathVariable Long studentId,
            @RequestParam Long roomId) {
        return ResponseEntity.ok(allocationService.reallocateStudent(studentId, roomId));
    }

    @PostMapping("/waitlist/promote")
    public ResponseEntity<Void> promoteFromWaitlist(
            @RequestParam Long roomId,
            @RequestParam int cycleId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));
        allocationService.promoteFromWaitlist(room, cycleId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/reset/{cycleId}")
    public ResponseEntity<String> resetCycle(@PathVariable int cycleId)
    {
        allocationService.resetCycle(cycleId);
        return ResponseEntity.ok("Cycle " + cycleId + " cleared. Ready to re-run.");
    }
}