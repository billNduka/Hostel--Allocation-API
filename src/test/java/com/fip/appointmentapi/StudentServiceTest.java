package com.fip.appointmentapi;

import com.fip.appointmentapi.entity.Gender;
import com.fip.appointmentapi.entity.Student;
import com.fip.appointmentapi.exception.DuplicateResourceException;
import com.fip.appointmentapi.exception.ResourceNotFoundException;
import com.fip.appointmentapi.repository.AllocationRepository;
import com.fip.appointmentapi.repository.StudentRepository;
import com.fip.appointmentapi.service.StudentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private AllocationRepository allocationRepository;

    @InjectMocks
    private StudentService studentService;

    private Student student;

    @BeforeEach
    void setUp() {
        student = new Student("Ada Obi", "CSC/2021/001", Gender.FEMALE, 3);
        student.setId(1L); // simulate already saved
    }

    @Test
    void createStudent_savesSuccessfully_whenMatricNumberIsUnique() {
        when(studentRepository.findByMatricNumber("CSC/2021/001"))
                .thenReturn(Optional.empty());
        when(studentRepository.save(student)).thenReturn(student);

        Student result = studentService.createStudent(student);

        assertThat(result.getName()).isEqualTo("Ada Obi");
        verify(studentRepository).save(student);
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when matric number already exists")
    void createStudent_throwsDuplicateException_whenMatricNumberExists() {
        // Arrange
        when(studentRepository.findByMatricNumber("CSC/2021/001"))
                .thenReturn(Optional.of(student));

        // Act & Assert
        assertThatThrownBy(() -> studentService.createStudent(student))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("CSC/2021/001")
                .hasMessageContaining("already exists");

        verify(studentRepository, times(1)).findByMatricNumber("CSC/2021/001");
        verify(studentRepository, never()).save(any()); // Changed from times(1) to never()
    }

    @Test
    void getStudentById_returnsStudent_whenExists() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));

        Student result = studentService.getStudentById(1L);

        assertThat(result.getMatricNumber()).isEqualTo("CSC/2021/001");
    }

    @Test
    void getStudentById_throwsNotFoundException_whenMissing() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.getStudentById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void updateStudent_doesNotChangeMatricNumber() {
        Student updatedDetails = new Student("Ada Updated", "DIFFERENT/001", Gender.FEMALE, 4);

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(studentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Student result = studentService.updateStudent(1L, updatedDetails);

        assertThat(result.getName()).isEqualTo("Ada Updated");
        assertThat(result.getMatricNumber()).isEqualTo("CSC/2021/001"); // unchanged
    }
}