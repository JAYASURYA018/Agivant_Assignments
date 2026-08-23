package com.example.leavemanagement.service;

import com.example.leavemanagement.dto.*;
import com.example.leavemanagement.entity.*;
import com.example.leavemanagement.enums.*;
import com.example.leavemanagement.exception.BusinessException;
import com.example.leavemanagement.repository.*;
import java.time.LocalDate;
import java.util.*;
import org.junit.jupiter.api.*;
import org.mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LeaveRequestServiceTest {

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private com.example.leavemanagement.repository.HolidayRepository holidayRepository;

    @InjectMocks
    private LeaveRequestService leaveRequestService;

    private Employee mockEmployee;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockEmployee = new Employee();
        mockEmployee.setId(1L);
        mockEmployee.setFirstName("Rahul");
        mockEmployee.setLastName("Sharma");
        mockEmployee.setEmail("rahul.sharma@example.com");
    }

    @Test
    void createLeaveRequest_Success_CalculatesDays() {
        LeaveRequestDto dto = new LeaveRequestDto(1L, LeaveType.CL, LocalDate.of(2026, 8, 10), LocalDate.of(2026, 8, 12), "Family event");
        
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(mockEmployee));
        when(leaveRequestRepository.findByEmployeeId(1L)).thenReturn(Collections.emptyList());
        
        LeaveRequest savedRequest = new LeaveRequest();
        savedRequest.setId(10L);
        savedRequest.setEmployee(mockEmployee);
        savedRequest.setLeaveType(LeaveType.CL);
        savedRequest.setStartDate(dto.getStartDate());
        savedRequest.setEndDate(dto.getEndDate());
        savedRequest.setNumberOfDays(3); 
        savedRequest.setStatus(LeaveStatus.PENDING);
        savedRequest.setReason("Family event");

        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(savedRequest);

        LeaveResponse response = leaveRequestService.createLeaveRequest(dto);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals(3, response.getNumberOfDays()); 
        assertEquals(LeaveStatus.PENDING, response.getStatus());
        verify(leaveRequestRepository, times(1)).save(any(LeaveRequest.class));
    }

    @Test
    void createLeaveRequest_Success_ExcludesWeekends() {
        
        LeaveRequestDto dto = new LeaveRequestDto(1L, LeaveType.CL, LocalDate.of(2026, 8, 7), LocalDate.of(2026, 8, 10), "Weekend trip");
        
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(mockEmployee));
        when(leaveRequestRepository.findByEmployeeId(1L)).thenReturn(Collections.emptyList());
        
        LeaveRequest savedRequest = new LeaveRequest();
        savedRequest.setId(11L);
        savedRequest.setEmployee(mockEmployee);
        savedRequest.setLeaveType(LeaveType.CL);
        savedRequest.setStartDate(dto.getStartDate());
        savedRequest.setEndDate(dto.getEndDate());
        savedRequest.setNumberOfDays(2); 
        savedRequest.setStatus(LeaveStatus.PENDING);

        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(savedRequest);

        LeaveResponse response = leaveRequestService.createLeaveRequest(dto);

        assertNotNull(response);
        assertEquals(2, response.getNumberOfDays()); 
    }

    @Test
    void createLeaveRequest_InvalidDates_ThrowsBusinessException() {
        LeaveRequestDto dto = new LeaveRequestDto(1L, LeaveType.CL, LocalDate.of(2026, 8, 15), LocalDate.of(2026, 8, 12), "Error dates");
        
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(mockEmployee));

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            leaveRequestService.createLeaveRequest(dto);
        });

        assertEquals("Start date cannot be after end date.", exception.getMessage());
        verify(leaveRequestRepository, never()).save(any(LeaveRequest.class));
    }

    @Test
    void createLeaveRequest_OverlappingDates_ThrowsBusinessException() {
        LeaveRequestDto dto = new LeaveRequestDto(1L, LeaveType.CL, LocalDate.of(2026, 8, 10), LocalDate.of(2026, 8, 12), "Overlap attempt");
        
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(mockEmployee));

        
        LeaveRequest existing = new LeaveRequest();
        existing.setId(1L);
        existing.setEmployee(mockEmployee);
        existing.setStartDate(LocalDate.of(2026, 8, 9));
        existing.setEndDate(LocalDate.of(2026, 8, 11));
        existing.setStatus(LeaveStatus.APPROVED);

        when(leaveRequestRepository.findByEmployeeId(1L)).thenReturn(Collections.singletonList(existing));

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            leaveRequestService.createLeaveRequest(dto);
        });

        assertTrue(exception.getMessage().contains("overlap"));
        verify(leaveRequestRepository, never()).save(any(LeaveRequest.class));
    }

    @Test
    void approveLeaveRequest_Success() {
        LeaveRequest req = new LeaveRequest();
        req.setId(10L);
        req.setEmployee(mockEmployee);
        req.setStatus(LeaveStatus.PENDING);

        when(leaveRequestRepository.findById(10L)).thenReturn(Optional.of(req));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(i -> i.getArguments()[0]);

        LeaveResponse response = leaveRequestService.approveLeaveRequest(10L);

        assertNotNull(response);
        assertEquals(LeaveStatus.APPROVED, response.getStatus());
    }

    @Test
    void approveLeaveRequest_CancelledRequest_ThrowsBusinessException() {
        LeaveRequest req = new LeaveRequest();
        req.setId(10L);
        req.setStatus(LeaveStatus.CANCELLED);

        when(leaveRequestRepository.findById(10L)).thenReturn(Optional.of(req));

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            leaveRequestService.approveLeaveRequest(10L);
        });

        assertEquals("Cannot approve a cancelled leave request.", exception.getMessage());
    }

    @Test
    void rejectLeaveRequest_Success() {
        LeaveRequest req = new LeaveRequest();
        req.setId(10L);
        req.setStatus(LeaveStatus.PENDING);

        when(leaveRequestRepository.findById(10L)).thenReturn(Optional.of(req));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(i -> i.getArguments()[0]);

        LeaveResponse response = leaveRequestService.rejectLeaveRequest(10L);

        assertNotNull(response);
        assertEquals(LeaveStatus.REJECTED, response.getStatus());
    }

    @Test
    void rejectLeaveRequest_ApprovedRequest_ThrowsBusinessException() {
        LeaveRequest req = new LeaveRequest();
        req.setId(10L);
        req.setStatus(LeaveStatus.APPROVED);

        when(leaveRequestRepository.findById(10L)).thenReturn(Optional.of(req));

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            leaveRequestService.rejectLeaveRequest(10L);
        });

        assertEquals("An approved leave request cannot be rejected.", exception.getMessage());
    }

    @Test
    void cancelLeaveRequest_Success() {
        LeaveRequest req = new LeaveRequest();
        req.setId(10L);
        req.setStatus(LeaveStatus.PENDING);

        when(leaveRequestRepository.findById(10L)).thenReturn(Optional.of(req));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(i -> i.getArguments()[0]);

        LeaveResponse response = leaveRequestService.cancelLeaveRequest(10L);

        assertNotNull(response);
        assertEquals(LeaveStatus.CANCELLED, response.getStatus());
    }

    @Test
    void cancelLeaveRequest_RejectedRequest_ThrowsBusinessException() {
        LeaveRequest req = new LeaveRequest();
        req.setId(10L);
        req.setStatus(LeaveStatus.REJECTED);

        when(leaveRequestRepository.findById(10L)).thenReturn(Optional.of(req));

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            leaveRequestService.cancelLeaveRequest(10L);
        });

        assertEquals("A rejected leave request cannot be cancelled.", exception.getMessage());
    }

    @Test
    void createLeaveRequest_ExcludesWeekendsAndHolidays() {
        
        
        
        
        LocalDate start = LocalDate.of(2026, 9, 11);
        LocalDate end = LocalDate.of(2026, 9, 15);
        LeaveRequestDto dto = new LeaveRequestDto(1L, LeaveType.CL, start, end, "Ganesh Chaturthi gap");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(mockEmployee));
        when(leaveRequestRepository.findByEmployeeId(1L)).thenReturn(Collections.emptyList());

        com.example.leavemanagement.entity.Holiday chaturthi = new com.example.leavemanagement.entity.Holiday();
        chaturthi.setHolidayDate(LocalDate.of(2026, 9, 14));
        when(holidayRepository.findAll()).thenReturn(Collections.singletonList(chaturthi));

        LeaveRequest savedRequest = new LeaveRequest();
        savedRequest.setId(12L);
        savedRequest.setEmployee(mockEmployee);
        savedRequest.setLeaveType(LeaveType.CL);
        savedRequest.setStartDate(start);
        savedRequest.setEndDate(end);
        savedRequest.setNumberOfDays(2);
        savedRequest.setStatus(LeaveStatus.PENDING);

        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(savedRequest);

        LeaveResponse response = leaveRequestService.createLeaveRequest(dto);

        assertNotNull(response);
        assertEquals(2, response.getNumberOfDays());
    }
}
