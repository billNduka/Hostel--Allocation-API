package com.fip.appointmentapi.repository;

import com.fip.appointmentapi.entity.Hostel;
import com.fip.appointmentapi.entity.Gender;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface HostelRepository extends JpaRepository<Hostel, Long>
{
    List<Hostel> findByGender(Gender gender);
    Optional<Hostel> findByName(String name);
}