package com.fip.appointmentapi.service;

import com.fip.appointmentapi.entity.Gender;
import com.fip.appointmentapi.entity.Student;
import com.fip.appointmentapi.entity.Allocation;
import com.fip.appointmentapi.exception.DuplicateResourceException;
import com.fip.appointmentapi.exception.ResourceNotFoundException;
import com.fip.appointmentapi.repository.StudentRepository;
import com.fip.appointmentapi.repository.AllocationRepository;
import com.fip.appointmentapi.entity.AllocationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import com.fip.appointmentapi.dto.StudentCsvRecord;
import com.opencsv.bean.CsvToBeanBuilder;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;


@Service
@RequiredArgsConstructor
public class StudentService
{

    private final StudentRepository studentRepository;
    private final AllocationRepository allocationRepository;

    public Student createStudent(Student student) {
        if (studentRepository.findByMatricNumber(student.getMatricNumber()).isPresent()) {
            throw new DuplicateResourceException(
                    "Student with matric number " + student.getMatricNumber() + " already exists"
            );
        }
        return studentRepository.save(student);
    }

    public List<Student> getAllStudents()
    {
        return studentRepository.findAll();
    }

    public Student getStudentById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", id));
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

    public List<Student> batchCreateStudents(List<Student> students) {
        List<Student> saved = new ArrayList<>();
        List<String> skipped = new ArrayList<>();

        for (Student student : students) {
            if (studentRepository.findByMatricNumber(student.getMatricNumber()).isPresent()) {
                skipped.add(student.getMatricNumber());
                continue;
            }
            saved.add(studentRepository.save(student));
        }

        if (!skipped.isEmpty()) {
            System.out.println("Skipped existing matric numbers: " + skipped);
        }

        return saved;
    }

    public List<Student> importFromCsv(MultipartFile file) {
        try (Reader reader = new InputStreamReader(file.getInputStream())) {

            List<StudentCsvRecord> records = new CsvToBeanBuilder<StudentCsvRecord>(reader)
                    .withType(StudentCsvRecord.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build()
                    .parse();

            List<Student> students = records.stream()
                    .map(r -> new Student(
                            r.getName(),
                            r.getMatricNumber(),
                            Gender.valueOf(r.getGender().toUpperCase()),
                            r.getYearOfStudy()
                    ))
                    .toList();

            return batchCreateStudents(students);

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse CSV file: " + e.getMessage());
        }
    }
}