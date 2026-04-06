package com.fip.appointmentapi;

import com.fip.appointmentapi.entity.*;
import com.fip.appointmentapi.repository.AllocationRepository;
import com.fip.appointmentapi.repository.RoomRepository;
import com.fip.appointmentapi.service.strategy.FirstComeFirstServedStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FirstComeFirstServedStrategy Tests")
class FirstComeFirstServedStrategyTest
{

    @Mock private RoomRepository roomRepository;
    @Mock private AllocationRepository allocationRepository;

    private FirstComeFirstServedStrategy strategy;

    private Student student1, student2, student3;
    private Room room;
    private Hostel hostel;

    @BeforeEach
    void setUp()
    {
        strategy = new FirstComeFirstServedStrategy(roomRepository, allocationRepository);

        hostel = new Hostel("Moremi Hall", "South Campus", Gender.FEMALE);
        hostel.setId(1L);

        // ID order matters — student1 registered first
        student1 = new Student("Ada Obi", "CSC/001", Gender.FEMALE, 3);
        student1.setId(1L);
        
        student2 = new Student("Bola Ade", "CSC/002", Gender.FEMALE, 2);
        student2.setId(2L);
        
        student3 = new Student("Chidi Nwa", "CSC/003", Gender.FEMALE, 4);
        student3.setId(3L);

        room = new Room(hostel, "A101", 2);
        room.setId(1L);
        room.setOccupied(0);
    }

    @Test
    @DisplayName("Should allocate students in ID order (first-come-first-served)")
    void allocate_assignsInIdOrder()
    {
        // Arrange
        when(allocationRepository.existsByStudentIdAndStatus(anyLong(), any(AllocationStatus.class)))
                .thenReturn(false);
        when(allocationRepository.save(any(Allocation.class))).thenAnswer(i -> i.getArgument(0));
        when(roomRepository.save(any(Room.class))).thenAnswer(i -> i.getArgument(0));

        // Act - intentionally shuffled to test ordering
        List<Allocation> results = strategy.allocate(
                List.of(student3, student1, student2),
                List.of(room),
                1
        );

        // Assert
        List<Allocation> allocated = results.stream()
                .filter(a -> a.getStatus() == AllocationStatus.ALLOCATED)
                .toList();
        List<Allocation> waitlisted = results.stream()
                .filter(a -> a.getStatus() == AllocationStatus.WAITLISTED)
                .toList();

        assertThat(results).hasSize(3);
        assertThat(allocated).hasSize(2); // room capacity is 2
        assertThat(waitlisted).hasSize(1);

        // Student1 (id=1) and student2 (id=2) should be allocated first
        assertThat(allocated.get(0).getStudent().getId()).isEqualTo(1L);
        assertThat(allocated.get(0).getRoom()).isEqualTo(room);
        assertThat(allocated.get(1).getStudent().getId()).isEqualTo(2L);
        assertThat(allocated.get(1).getRoom()).isEqualTo(room);
        
        // Student3 (id=3) should be waitlisted
        assertThat(waitlisted.get(0).getStudent().getId()).isEqualTo(3L);
        assertThat(waitlisted.get(0).getRoom()).isNull();
        assertThat(waitlisted.get(0).getWaitlistPosition()).isEqualTo(1);
        
        verify(allocationRepository, times(3)).save(any(Allocation.class));
        verify(roomRepository, times(2)).save(room); // saved twice for the 2 allocations
    }

    @Test
    @DisplayName("Should skip students already allocated")
    void allocate_skipsAlreadyAllocatedStudents()
    {
        // Arrange
        when(allocationRepository.existsByStudentIdAndStatus(1L, AllocationStatus.ALLOCATED))
                .thenReturn(true); // student1 already allocated
        when(allocationRepository.existsByStudentIdAndStatus(2L, AllocationStatus.ALLOCATED))
                .thenReturn(false);
        when(allocationRepository.existsByStudentIdAndStatus(3L, AllocationStatus.ALLOCATED))
                .thenReturn(false);
        when(allocationRepository.save(any(Allocation.class))).thenAnswer(i -> i.getArgument(0));
        when(roomRepository.save(any(Room.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        List<Allocation> results = strategy.allocate(
                List.of(student1, student2, student3),
                List.of(room),
                1
        );

        // Assert
        assertThat(results).hasSize(2); // student1 skipped entirely
        assertThat(results.stream()
                .noneMatch(a -> a.getStudent().getId().equals(1L))).isTrue();
        assertThat(results.stream()
                .anyMatch(a -> a.getStudent().getId().equals(2L))).isTrue();
        assertThat(results.stream()
                .anyMatch(a -> a.getStudent().getId().equals(3L))).isTrue();
        
        verify(allocationRepository, times(3)).existsByStudentIdAndStatus(anyLong(), eq(AllocationStatus.ALLOCATED));
        verify(allocationRepository, times(2)).save(any(Allocation.class)); // only student2 and student3
    }

    @Test
    @DisplayName("Should waitlist all students when no rooms available")
    void allocate_waitlistsStudents_whenNoRoomsAvailable()
    {
        // Arrange
        when(allocationRepository.existsByStudentIdAndStatus(anyLong(), any(AllocationStatus.class)))
                .thenReturn(false);
        when(allocationRepository.save(any(Allocation.class))).thenAnswer(i -> i.getArgument(0));

        // Act - no rooms provided
        List<Allocation> results = strategy.allocate(
                List.of(student1, student2, student3),
                List.of(),
                1
        );

        // Assert
        assertThat(results).hasSize(3);
        assertThat(results).allMatch(a -> a.getStatus() == AllocationStatus.WAITLISTED);
        assertThat(results).allMatch(a -> a.getRoom() == null);
        
        // Check waitlist positions are sequential
        assertThat(results.get(0).getWaitlistPosition()).isEqualTo(1);
        assertThat(results.get(1).getWaitlistPosition()).isEqualTo(2);
        assertThat(results.get(2).getWaitlistPosition()).isEqualTo(3);
        
        verify(allocationRepository, times(3)).save(any(Allocation.class));
        verify(roomRepository, never()).save(any(Room.class));
    }

    @Test
    @DisplayName("Should respect gender constraint when allocating rooms")
    void allocate_respectsGenderConstraint()
    {
        // Arrange
        Student maleStudent = new Student("Emeka Joe", "ENG/001", Gender.MALE, 2);
        maleStudent.setId(4L);

        when(allocationRepository.existsByStudentIdAndStatus(anyLong(), any(AllocationStatus.class)))
                .thenReturn(false);
        when(allocationRepository.save(any(Allocation.class))).thenAnswer(i -> i.getArgument(0));
        when(roomRepository.save(any(Room.class))).thenAnswer(i -> i.getArgument(0));

        // Act - female room, one female and one male student
        List<Allocation> results = strategy.allocate(
                List.of(student1, maleStudent),
                List.of(room),
                1
        );

        // Assert
        List<Allocation> allocated = results.stream()
                .filter(a -> a.getStatus() == AllocationStatus.ALLOCATED)
                .toList();
        List<Allocation> waitlisted = results.stream()
                .filter(a -> a.getStatus() == AllocationStatus.WAITLISTED)
                .toList();

        assertThat(allocated).hasSize(1);
        assertThat(allocated.get(0).getStudent().getGender()).isEqualTo(Gender.FEMALE);
        assertThat(allocated.get(0).getStudent().getId()).isEqualTo(1L);
        
        assertThat(waitlisted).hasSize(1);
        assertThat(waitlisted.get(0).getStudent().getGender()).isEqualTo(Gender.MALE);
        assertThat(waitlisted.get(0).getStudent().getId()).isEqualTo(4L);
        
        verify(allocationRepository, times(2)).save(any(Allocation.class));
        verify(roomRepository, times(1)).save(room);
    }

    @Test
    @DisplayName("Should return strategy name correctly")
    void getStrategyName_returnsCorrectName()
    {
        // Act
        String name = strategy.getStrategyName();

        // Assert
        assertThat(name).isEqualTo("FIRST_COME_FIRST_SERVED");
    }

    @Test
    @DisplayName("Should fill room completely before moving to next room")
    void allocate_fillsRoomCompletelyBeforeMovingToNext()
    {
        // Arrange
        Room room2 = new Room(hostel, "A102", 2);
        room2.setId(2L);
        room2.setOccupied(0);

        Student student4 = new Student("Dupe Ola", "CSC/004", Gender.FEMALE, 1);
        student4.setId(4L);

        when(allocationRepository.existsByStudentIdAndStatus(anyLong(), any(AllocationStatus.class)))
                .thenReturn(false);
        when(allocationRepository.save(any(Allocation.class))).thenAnswer(i -> i.getArgument(0));
        when(roomRepository.save(any(Room.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        List<Allocation> results = strategy.allocate(
                List.of(student1, student2, student3, student4),
                List.of(room, room2),
                1
        );

        // Assert
        List<Allocation> allocated = results.stream()
                .filter(a -> a.getStatus() == AllocationStatus.ALLOCATED)
                .toList();

        assertThat(allocated).hasSize(4);

        // First 2 students should get room1
        assertThat(allocated.get(0).getRoom().getId()).isEqualTo(1L);
        assertThat(allocated.get(1).getRoom().getId()).isEqualTo(1L);

        // Next 2 students should get room2
        assertThat(allocated.get(2).getRoom().getId()).isEqualTo(2L);
        assertThat(allocated.get(3).getRoom().getId()).isEqualTo(2L);

        // Both rooms should be full
        assertThat(room.getOccupied()).isEqualTo(2);
        assertThat(room2.getOccupied()).isEqualTo(2);
    }
}