package com.fip.appointmentapi.repository;

import com.fip.appointmentapi.entity.Allocation;
import com.fip.appointmentapi.entity.AllocationStatus;
import com.fip.appointmentapi.entity.Gender;
import com.fip.appointmentapi.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long>
{
    Optional<Student> findByMatricNumber(String matricNumber);

    Optional<Student> findByYearOfStudy(int yearOfStudy);
    Optional<Student> findByGender(Gender gender);
}