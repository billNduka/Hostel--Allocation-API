package com.fip.appointmentapi.service;

import com.fip.appointmentapi.entity.Hostel;
import com.fip.appointmentapi.entity.Room;
import com.fip.appointmentapi.repository.HostelRepository;
import com.fip.appointmentapi.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HostelService 
{

    private final HostelRepository hostelRepository;
    private final RoomRepository roomRepository;

    public Hostel createHostel(Hostel hostel) 
    {
        return hostelRepository.save(hostel);
    }

    public List<Hostel> getAllHostels() 
    {
        return hostelRepository.findAll();
    }

    public Hostel getHostelById(Long id) 
    {
        return hostelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hostel not found with id: " + id));
    }

    public Hostel updateHostel(Long id, Hostel hostelDetails) 
    {
        Hostel hostel = getHostelById(id);
        hostel.setName(hostelDetails.getName());
        hostel.setLocation(hostelDetails.getLocation());
        hostel.setGender(hostelDetails.getGender());
        return hostelRepository.save(hostel);
    }

    public void deleteHostel(Long id) 
    {
        Hostel hostel = getHostelById(id);
        hostelRepository.delete(hostel);
    }

    public List<Room> getRoomsByHostel(Long hostelId) 
    {
        getHostelById(hostelId); // verifies hostel exists before querying rooms
        return roomRepository.findByHostelId(hostelId);
    }
}