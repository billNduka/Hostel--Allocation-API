package com.fip.appointmentapi.service;

import com.fip.appointmentapi.entity.Student;
import com.fip.appointmentapi.entity.Allocation;
import com.fip.appointmentapi.repository.StudentRepository;
import com.fip.appointmentapi.repository.AllocationRepository;
import com.fip.appointmentapi.entity.AllocationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StudentService
{

    private final StudentRepository studentRepository;
    private final AllocationRepository allocationRepository;

    public Student createStudent(Student student) {
        if (studentRepository.findByMatricNumber(student.getMatricNumber()).isPresent())
        {
            throw new RuntimeException("Student with matric number " + student.getMatricNumber() + " already exists");
        }
        return studentRepository.save(student);
    }

    public List<Student> getAllStudents()
    {
        return studentRepository.findAll();
    }

    public Student getStudentById(Long id)
    {
        return studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + id));
    }

    public Student getStudentByMatricNumber(String matricNumber)
    {
        return studentRepository.findByMatricNumber(matricNumber)
                .orElseThrow(() -> new RuntimeException("Student not found with matric number: " + matricNumber));
    }

    public Student updateStudent(Long id, Student studentDetails)
    {
        Student student = getStudentById(id);
        student.setName(studentDetails.getName());
        student.setGender(studentDetails.getGender());
        // matric number intentionally not updatable — it's an identifier
        return studentRepository.save(student);
    }

    public void deleteStudent(Long id)
    {
        Student student = getStudentById(id);
        studentRepository.delete(student);
    }

    public Optional<Allocation> getCurrentAllocation(Long studentId)
    {
        getStudentById(studentId); // verify student exists first
        return allocationRepository.findByStudentIdAndStatus(studentId, AllocationStatus.ALLOCATED);
    }
}