package com.example.leavemanagement.service;

import com.example.leavemanagement.dto.*;
import com.example.leavemanagement.entity.*;
import com.example.leavemanagement.enums.*;
import com.example.leavemanagement.repository.*;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DashboardServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getDashboardStats_CalculatesAccrualsAndCarryForward() {
        Long empId = 1L;
        
        Employee emp = new Employee();
        emp.setId(empId);
        emp.setFirstName("John");
        emp.setLastName("Doe");
        emp.setCreatedAt(LocalDateTime.of(2025, 6, 1, 12, 0));

        when(employeeRepository.findById(empId)).thenReturn(Optional.of(emp));
        when(employeeRepository.count()).thenReturn(10L);
        when(leaveRequestRepository.countPendingRequests()).thenReturn(2L);
        when(leaveRequestRepository.sumApprovedDaysForEmployeeAndYear(eq(empId), any(LocalDate.class), any(LocalDate.class))).thenReturn(4L);

        LeaveRequest pastEL = new LeaveRequest();
        pastEL.setLeaveType(LeaveType.EL);
        pastEL.setStatus(LeaveStatus.APPROVED);
        pastEL.setStartDate(LocalDate.of(2025, 7, 1));
        pastEL.setEndDate(LocalDate.of(2025, 7, 3));
        pastEL.setNumberOfDays(3);

        LeaveRequest curCL = new LeaveRequest();
        curCL.setLeaveType(LeaveType.CL);
        curCL.setStatus(LeaveStatus.APPROVED);
        curCL.setStartDate(LocalDate.of(2026, 3, 1));
        curCL.setEndDate(LocalDate.of(2026, 3, 2));
        curCL.setNumberOfDays(2);

        LeaveRequest curEL = new LeaveRequest();
        curEL.setLeaveType(LeaveType.EL);
        curEL.setStatus(LeaveStatus.APPROVED);
        curEL.setStartDate(LocalDate.of(2026, 4, 1));
        curEL.setEndDate(LocalDate.of(2026, 4, 4));
        curEL.setNumberOfDays(4);

        when(leaveRequestRepository.findByEmployeeId(empId)).thenReturn(Arrays.asList(pastEL, curCL, curEL));

        DashboardStatsResponse stats = dashboardService.getDashboardStats(empId);

        assertNotNull(stats);
        assertEquals("John Doe", stats.getEmployeeName());
        assertEquals(5.0, stats.getSickLeaveAvailable());
        assertEquals(0.0, stats.getSickLeaveBooked());

        int currentMonth = LocalDate.now().getMonthValue();
        assertEquals(2.0, stats.getCasualLeaveBooked());
        assertEquals(Math.max(0, (currentMonth * 1.0) - 2), stats.getCasualLeaveAvailable());
        assertEquals(4.0, stats.getEarnedLeaveBooked());
        assertEquals(Math.max(0, 12.0 + (currentMonth * 1.25) - 4), stats.getEarnedLeaveAvailable());
    }

    @Test
    void getReports_CalculateCorrectSummaries() {
        
        Employee e1 = new Employee(1L, "EMP1", "Alice", "Smith", "alice@example.com", "pass", "Engineering", "EMPLOYEE", LocalDateTime.now());
        Employee e2 = new Employee(2L, "EMP2", "Bob", "Jones", "bob@example.com", "pass", "Sales", "EMPLOYEE", LocalDateTime.now());

        when(employeeRepository.findAll()).thenReturn(Arrays.asList(e1, e2));

        
        LeaveRequest r1 = new LeaveRequest();
        r1.setId(1L);
        r1.setEmployee(e1);
        r1.setStatus(LeaveStatus.APPROVED);
        r1.setLeaveType(LeaveType.CL);
        r1.setNumberOfDays(6);

        LeaveRequest r2 = new LeaveRequest();
        r2.setId(2L);
        r2.setEmployee(e1);
        r2.setStatus(LeaveStatus.PENDING); 
        r2.setLeaveType(LeaveType.EL);
        r2.setNumberOfDays(4);

        when(leaveRequestRepository.findAll()).thenReturn(Arrays.asList(r1, r2));

        
        List<TotalLeavesReportResponse> totalLeaves = dashboardService.getTotalLeavesReport(5);
        assertEquals(1, totalLeaves.size());
        assertEquals("EMP1", totalLeaves.get(0).getEmployeeId());
        assertEquals(6.0, totalLeaves.get(0).getTotalDays());

        
        List<NoLeavesReportResponse> noLeaves = dashboardService.getNoLeavesReport();
        assertEquals(1, noLeaves.size());
        assertEquals("EMP2", noLeaves.get(0).getEmployeeId());
        assertEquals("Bob Jones", noLeaves.get(0).getName());

        
        List<LeaveTypeReportResponse> freqTypes = dashboardService.getFrequentTypeReport();
        assertEquals(3, freqTypes.size());
        
        LeaveTypeReportResponse clRep = freqTypes.stream().filter(f -> f.getLeaveType().equals("CL")).findFirst().get();
        assertEquals(1, clRep.getCount());
        assertEquals(6.0, clRep.getDays());
        assertTrue(clRep.getIsWinner());
    }
}
