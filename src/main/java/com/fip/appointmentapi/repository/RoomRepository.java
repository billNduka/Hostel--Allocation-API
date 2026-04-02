package com.fip.appointmentapi.repository;

import com.fip.appointmentapi.entity.Gender;
import com.fip.appointmentapi.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long>
{
    List<Room> findByHostelId(Long hostelId);
    List<Room> findByCapacity(int capacity);

    @Query("SELECT r FROM Room r WHERE r.occupied < r.capacity")
    List<Room> findAvailableRooms();

    @Query("SELECT r FROM Room r WHERE r.occupied < r.capacity AND r.hostel.id = :hostelId")
    List<Room> findAvailableRoomsByHostel(Long hostelId);

    @Query("SELECT r FROM Room r WHERE r.occupied < r.capacity AND r.hostel.gender = :gender")
    List<Room> findAvailableRoomsByGender(Gender gender);
}