package com.fip.appointmentapi.service;

import com.fip.appointmentapi.entity.Hostel;
import com.fip.appointmentapi.entity.Room;

import java.util.List;

abstract class HostelService
{
    abstract List<Hostel> getAllHostels();
    abstract Hostel getHostelById(Long id);
    abstract Hostel updateHostel(Long id, Hostel hostelDetails);
    abstract void deleteHostel(Long id);
    abstract List<Room> getRoomsByHostel(Long hostelId);
}