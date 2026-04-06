package com.fip.appointmentapi.controller;

import com.fip.appointmentapi.entity.Room;
import com.fip.appointmentapi.entity.Gender;
import com.fip.appointmentapi.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController
{

    private final RoomService roomService;

    @PostMapping
    public ResponseEntity<Room> createRoom(@RequestBody Room room)
    {
        return ResponseEntity.status(HttpStatus.CREATED).body(roomService.createRoom(room));
    }

    @GetMapping
    public ResponseEntity<List<Room>> getAllRooms()
    {
        return ResponseEntity.ok(roomService.getAllRooms());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Room> getRoomById(@PathVariable Long id)
    {
        return ResponseEntity.ok(roomService.getRoomById(id));
    }

    @GetMapping("/hostel/{hostelId}")
    public ResponseEntity<List<Room>> getRoomsByHostel(@PathVariable Long hostelId)
    {
        return ResponseEntity.ok(roomService.getRoomsByHostel(hostelId));
    }

    @GetMapping("/available")
    public ResponseEntity<List<Room>> getAvailableRooms()
    {
        return ResponseEntity.ok(roomService.getAvailableRooms());
    }

    @GetMapping("/available/gender/{gender}")
    public ResponseEntity<List<Room>> getAvailableRoomsByGender(@PathVariable Gender gender)
    {
        return ResponseEntity.ok(roomService.getAvailableRoomsByGender(gender));
    }

    @GetMapping("/capacity/{capacity}")
    public ResponseEntity<List<Room>> getRoomsByCapacity(@PathVariable int capacity)
    {
        return ResponseEntity.ok(roomService.getRoomsByCapacity(capacity));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Room> updateRoom(@PathVariable Long id, @RequestBody Room roomDetails)
    {
        return ResponseEntity.ok(roomService.updateRoom(id, roomDetails));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id)
    {
        roomService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/batch")
    public ResponseEntity<List<Room>> batchCreateRooms(
            @RequestBody List<Room> rooms) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(roomService.batchCreateRooms(rooms));
    }
}