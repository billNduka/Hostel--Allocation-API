package com.fip.appointmentapi.controller;

import com.fip.appointmentapi.entity.Hostel;
import com.fip.appointmentapi.entity.Room;
import com.fip.appointmentapi.service.HostelService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hostels")
public class HostelController {

    private final HostelService hostelService;

    public HostelController(HostelService hostelService) {
        this.hostelService = hostelService;
    }

    // POST /api/hostels — create a hostel
    @PostMapping
    public ResponseEntity<Hostel> createHostel(@RequestBody Hostel hostel) {
        Hostel created = hostelService.createHostel(hostel);
        return ResponseEntity.ok(created);
    }

    // GET /api/hostels — list all hostels
    @GetMapping
    public ResponseEntity<List<Hostel>> getAllHostels() {
        return ResponseEntity.ok(hostelService.getAllHostels());
    }

    // GET /api/hostels/{id} — get one hostel
    @GetMapping("/{id}")
    public ResponseEntity<Hostel> getHostelById(@PathVariable Long id) {
        return ResponseEntity.ok(hostelService.getHostelById(id));
    }

    // PUT /api/hostels/{id} — update hostel details
    @PutMapping("/{id}")
    public ResponseEntity<Hostel> updateHostel(
            @PathVariable Long id,
            @RequestBody Hostel hostelDetails) {

        Hostel updated = hostelService.updateHostel(id, hostelDetails);
        return ResponseEntity.ok(updated);
    }

    // DELETE /api/hostels/{id} — remove a hostel
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHostel(@PathVariable Long id) {
        hostelService.deleteHostel(id);
        return ResponseEntity.noContent().build();
    }

    // GET /api/hostels/{id}/rooms — get all rooms in a hostel
    @GetMapping("/{id}/rooms")
    public ResponseEntity<List<Room>> getRoomsByHostel(@PathVariable Long id) {
        return ResponseEntity.ok(hostelService.getRoomsByHostel(id));
    }
}