package com.fip.appointmentapi.service;

import com.fip.appointmentapi.dto.RoomCreateRequest;
import com.fip.appointmentapi.entity.Gender;
import com.fip.appointmentapi.entity.Hostel;
import com.fip.appointmentapi.entity.Room;
import com.fip.appointmentapi.exception.ResourceNotFoundException;
import com.fip.appointmentapi.repository.HostelRepository;
import com.fip.appointmentapi.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomService
{

    private final RoomRepository roomRepository;
    private final HostelRepository hostelRepository;

    public Room createRoom(RoomCreateRequest request)
    {
        Hostel hostel = hostelRepository.findById(request.getHostelId())
                                        .orElseThrow(() -> new ResourceNotFoundException("Hostel", request.getHostelId()));
        
        Room room = new Room(hostel, request.getRoomNumber(), request.getCapacity());
        return roomRepository.save(room);
    }

    public List<Room> getAllRooms()
    {
        return roomRepository.findAll();
    }

    public Room getRoomById(Long id)
    {
        return roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room", id));
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

    public List<Room> batchCreateRooms(List<RoomCreateRequest> requests) {
        List<Room> saved = new ArrayList<>();

        for (RoomCreateRequest request : requests) {
            Hostel hostel = hostelRepository.findById(request.getHostelId())
                    .orElseThrow(() -> new ResourceNotFoundException("Hostel", request.getHostelId()));
            
            Room room = new Room(hostel, request.getRoomNumber(), request.getCapacity());
            saved.add(roomRepository.save(room));
        }

        return saved;
    }
}