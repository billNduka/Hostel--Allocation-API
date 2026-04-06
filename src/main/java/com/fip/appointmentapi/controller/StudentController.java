package com.fip.appointmentapi.controller;

import com.fip.appointmentapi.entity.Student;
import com.fip.appointmentapi.entity.Allocation;
import com.fip.appointmentapi.exception.InvalidAllocationException;
import com.fip.appointmentapi.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController
{

    private final StudentService studentService;

    @PostMapping
    public ResponseEntity<Student> createStudent(@RequestBody Student student)
    {
        return ResponseEntity.status(HttpStatus.CREATED).body(studentService.createStudent(student));
    }

    @GetMapping
    public ResponseEntity<List<Student>> getAllStudents()
    {
        return ResponseEntity.ok(studentService.getAllStudents());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Student> getStudentById(@PathVariable Long id)
    {
        return ResponseEntity.ok(studentService.getStudentById(id));
    }

    @GetMapping("/matric/{matricNumber}")
    public ResponseEntity<Student> getStudentByMatricNumber(@PathVariable String matricNumber)
    {
        return ResponseEntity.ok(studentService.getStudentByMatricNumber(matricNumber));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Student> updateStudent(@PathVariable Long id, @RequestBody Student studentDetails)
    {
        return ResponseEntity.ok(studentService.updateStudent(id, studentDetails));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id)
    {
        studentService.deleteStudent(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/allocation")
    public ResponseEntity<Allocation> getCurrentAllocation(@PathVariable Long id)
    {
        return studentService.getCurrentAllocation(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/batch")
    public ResponseEntity<List<Student>> batchCreateStudents(@RequestBody List<Student> students)
    {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(studentService.batchCreateStudents(students));
    }

    @PostMapping("/import/csv")
    public ResponseEntity<List<Student>> importFromCsv(@RequestParam("file") MultipartFile file)
    {
        if (file == null || file.isEmpty())
        {
            throw new InvalidAllocationException("Uploaded file is empty or missing");
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(studentService.importFromCsv(file));
    }
}