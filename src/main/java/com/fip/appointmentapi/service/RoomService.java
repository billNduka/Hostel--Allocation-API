package com.fip.appointmentapi.service;

import com.fip.appointmentapi.entity.Room;
import com.fip.appointmentapi.entity.Gender;
import com.fip.appointmentapi.repository.RoomRepository;
import com.fip.appointmentapi.repository.HostelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomService
{

    private final RoomRepository roomRepository;
    private final HostelRepository hostelRepository;

    public Room createRoom(Room room)
    {
        // verify hostel exists before saving room
        hostelRepository.findById(room.getHostel().getId())
                .orElseThrow(() -> new RuntimeException("Hostel not found with id: " + room.getHostel().getId()));
        return roomRepository.save(room);
    }

    public List<Room> getAllRooms()
    {
        return roomRepository.findAll();
    }

    public Room getRoomById(Long id)
    {
        return roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + id));
    }

    public List<Room> getRoomsByHostel(Long hostelId)
    {
        hostelRepository.findById(hostelId)
                .orElseThrow(() -> new RuntimeException("Hostel not found with id: " + hostelId));
        return roomRepository.findByHostelId(hostelId);
    }

    public List<Room> getAvailableRooms()
    {
        return roomRepository.findAvailableRooms();
    }

    public List<Room> getAvailableRoomsByGender(Gender gender)
    {
        return roomRepository.findAvailableRoomsByGender(gender);
    }

    public List<Room> getRoomsByCapacity(int capacity)
    {
        return roomRepository.findByCapacity(capacity);
    }

    public Room updateRoom(Long id, Room roomDetails)
    {
        Room room = getRoomById(id);
        room.setRoomNumber(roomDetails.getRoomNumber());
        room.setCapacity(roomDetails.getCapacity());
        return roomRepository.save(room);
    }

    public void deleteRoom(Long id)
    {
        Room room = getRoomById(id);
        roomRepository.delete(room);
    }
}