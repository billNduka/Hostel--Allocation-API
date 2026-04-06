package com.fip.appointmentapi;

import com.fip.appointmentapi.entity.*;
import com.fip.appointmentapi.exception.InvalidAllocationException;
import com.fip.appointmentapi.exception.ResourceNotFoundException;
import com.fip.appointmentapi.repository.*;
import com.fip.appointmentapi.service.AllocationService;
import com.fip.appointmentapi.service.AuditLogService;
import com.fip.appointmentapi.service.strategy.AllocationStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.fip.appointmentapi.entity.Gender.FEMALE;
import static com.fip.appointmentapi.entity.Gender.MALE;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AllocationService Tests")
class AllocationServiceTest {

    @Mock private AllocationRepository allocationRepository;
    @Mock private StudentRepository studentRepository;
    @Mock private RoomRepository roomRepository;
    @Mock private AuditLogService auditLogService;
    @Mock private AllocationStrategy mockStrategy;
    @Mock private AllocationCycleRepository cycleRepository;

    private AllocationService allocationService;

    private Student student;
    private Hostel hostel;
    private Room oldRoom, newRoom;
    private Allocation existingAllocation;

    @BeforeEach
    void setUp() {
        // Inject strategy map manually since @InjectMocks can't build a Map
        allocationService = new AllocationService(
                allocationRepository,
                studentRepository,
                roomRepository,
                auditLogService,
                cycleRepository,
                Map.of("FIRST_COME_FIRST_SERVED", mockStrategy)
        );
        
        // Set the active strategy after construction to avoid NullPointerException
        allocationService.setActiveStrategy("FIRST_COME_FIRST_SERVED");

        student = new Student("Ada Obi", "CSC/001", FEMALE, 3);
        student.setId(1L);

        hostel = new Hostel("Moremi Hall", "South Campus", FEMALE);
        hostel.setId(1L);

        oldRoom = new Room(hostel, "A101", 4);
        oldRoom.setId(1L);
        oldRoom.setOccupied(1);

        newRoom = new Room(hostel, "A102", 4);
        newRoom.setId(2L);
        newRoom.setOccupied(0);

        existingAllocation = new Allocation(student, oldRoom, AllocationStatus.ALLOCATED, 1);
        existingAllocation.setId(1L);
    }

    @Test
    @DisplayName("Should use active strategy when running allocation cycle")
    void runAllocationCycle_usesActiveStrategy()
    {
        AllocationCycle mockCycle = new AllocationCycle("FIRST_COME_FIRST_SERVED", 1);
        mockCycle.setId(1);  // simulate saved cycle with id

        when(studentRepository.findAll()).thenReturn(List.of(student));
        when(roomRepository.findAvailableRooms()).thenReturn(List.of(oldRoom));
        when(cycleRepository.save(any())).thenReturn(mockCycle);  // add this
        when(mockStrategy.allocate(any(), any(), anyInt()))
                .thenReturn(List.of(existingAllocation));
        when(auditLogService.log(any(), any(), any())).thenReturn(null);

        // Act
        List<Allocation> results = allocationService.runAllocationCycle();

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getStudent()).isEqualTo(student);
        assertThat(results.get(0).getRoom()).isEqualTo(oldRoom);

        verify(mockStrategy).allocate(any(), any(), eq(1));
        verify(studentRepository, times(1)).findAll();
        verify(roomRepository, times(1)).findAvailableRooms();
        verify(mockStrategy, times(1)).allocate(anyList(), anyList(), eq(1));
        verify(auditLogService, times(2)).log(anyString(), anyString(), anyInt()); // CYCLE_STARTED and CYCLE_COMPLETED
    }

    @Test
    @DisplayName("Should move student to new room successfully when reallocating")
    void reallocateStudent_movesStudentToNewRoom() {
        // Arrange
        when(allocationRepository.findByStudentIdAndStatus(1L, AllocationStatus.ALLOCATED))
                .thenReturn(Optional.of(existingAllocation));
        when(roomRepository.findById(2L)).thenReturn(Optional.of(newRoom));
        when(allocationRepository.findAll()).thenReturn(List.of(existingAllocation));
        when(allocationRepository.save(any(Allocation.class))).thenAnswer(i -> i.getArgument(0));
        when(allocationRepository.findTopByStatusOrderByWaitlistPositionAsc(AllocationStatus.WAITLISTED))
                .thenReturn(Optional.empty());
        when(auditLogService.log(anyString(), anyString(), anyInt())).thenReturn(null);

        // Act
        Allocation result = allocationService.reallocateStudent(1L, 2L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(AllocationStatus.ALLOCATED);
        assertThat(result.getRoom().getId()).isEqualTo(2L);
        assertThat(result.getStudent()).isEqualTo(student);
        assertThat(existingAllocation.getStatus()).isEqualTo(AllocationStatus.REALLOCATED);
        assertThat(oldRoom.getOccupied()).isEqualTo(0);
        assertThat(newRoom.getOccupied()).isEqualTo(1);
        
        verify(allocationRepository, times(1)).findByStudentIdAndStatus(1L, AllocationStatus.ALLOCATED);
        verify(roomRepository, times(1)).findById(2L);
        verify(allocationRepository, times(2)).save(any(Allocation.class)); // old and new allocations
        verify(roomRepository, times(2)).save(any(Room.class)); // old and new rooms
        verify(auditLogService, times(1)).log(eq("REALLOCATED"), anyString(), anyInt());
    }

    @Test
    @DisplayName("Should throw InvalidAllocationException when gender mismatch during reallocation")
    void reallocateStudent_throwsException_onGenderMismatch() {
        // Arrange
        Hostel maleHostel = new Hostel("Bello Hall", "North Campus", MALE);
        maleHostel.setId(2L);
        
        Room maleRoom = new Room(maleHostel, "B201", 4);
        maleRoom.setId(3L);
        maleRoom.setCapacity(4);
        maleRoom.setOccupied(0);

        when(allocationRepository.findByStudentIdAndStatus(1L, AllocationStatus.ALLOCATED))
                .thenReturn(Optional.of(existingAllocation));
        when(roomRepository.findById(3L)).thenReturn(Optional.of(maleRoom));

        // Act & Assert
        assertThatThrownBy(() -> allocationService.reallocateStudent(1L, 3L))
                .isInstanceOf(InvalidAllocationException.class)
                .hasMessageContaining("Gender mismatch")
                .hasMessageContaining("FEMALE")
                .hasMessageContaining("MALE");
        
        verify(allocationRepository, times(1)).findByStudentIdAndStatus(1L, AllocationStatus.ALLOCATED);
        verify(roomRepository, times(1)).findById(3L);
        verify(allocationRepository, never()).save(any());
        verify(auditLogService, never()).log(anyString(), anyString(), anyInt());
    }

    @Test
    @DisplayName("Should throw InvalidAllocationException when room is at full capacity")
    void reallocateStudent_throwsException_whenRoomFull() {
        // Arrange
        newRoom.setOccupied(4); // full capacity

        when(allocationRepository.findByStudentIdAndStatus(1L, AllocationStatus.ALLOCATED))
                .thenReturn(Optional.of(existingAllocation));
        when(roomRepository.findById(2L)).thenReturn(Optional.of(newRoom));

        // Act & Assert
        assertThatThrownBy(() -> allocationService.reallocateStudent(1L, 2L))
                .isInstanceOf(InvalidAllocationException.class)
                .hasMessageContaining("full capacity");
        
        verify(allocationRepository, times(1)).findByStudentIdAndStatus(1L, AllocationStatus.ALLOCATED);
        verify(roomRepository, times(1)).findById(2L);
        verify(allocationRepository, never()).save(any());
        verify(auditLogService, never()).log(anyString(), anyString(), anyInt());
    }

    @Test
    @DisplayName("Should throw InvalidAllocationException for unknown strategy")
    void setActiveStrategy_throwsException_forUnknownStrategy() {
        // Act & Assert
        assertThatThrownBy(() -> allocationService.setActiveStrategy("UNKNOWN"))
                .isInstanceOf(InvalidAllocationException.class)
                .hasMessageContaining("Unknown strategy")
                .hasMessageContaining("UNKNOWN");
    }

    @Test
    @DisplayName("Should set active strategy successfully when strategy exists")
    void setActiveStrategy_setsSuccessfully_whenStrategyExists() {
        // Act
        allocationService.setActiveStrategy("FIRST_COME_FIRST_SERVED");

        // Assert
        assertThat(allocationService.getActiveStrategy()).isEqualTo("FIRST_COME_FIRST_SERVED");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when allocation ID does not exist")
    void getAllocationById_throwsNotFoundException_whenMissing() {
        // Arrange
        when(allocationRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> allocationService.getAllocationById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Allocation")
                .hasMessageContaining("99");
        
        verify(allocationRepository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("Should return allocation when ID exists")
    void getAllocationById_returnsAllocation_whenExists() {
        // Arrange
        when(allocationRepository.findById(1L)).thenReturn(Optional.of(existingAllocation));

        // Act
        Allocation result = allocationService.getAllocationById(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStudent()).isEqualTo(student);
        assertThat(result.getRoom()).isEqualTo(oldRoom);
        
        verify(allocationRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should return all waitlisted allocations")
    void getWaitlistedAllocations_returnsWaitlist() {
        // Arrange
        Allocation waitlisted = new Allocation(student, null, AllocationStatus.WAITLISTED, 1);
        waitlisted.setWaitlistPosition(1);
        
        when(allocationRepository.findByStatus(AllocationStatus.WAITLISTED))
                .thenReturn(List.of(waitlisted));

        // Act
        List<Allocation> results = allocationService.getWaitlistedAllocations();

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getStatus()).isEqualTo(AllocationStatus.WAITLISTED);
        assertThat(results.get(0).getWaitlistPosition()).isEqualTo(1);
        
        verify(allocationRepository, times(1)).findByStatus(AllocationStatus.WAITLISTED);
    }
}