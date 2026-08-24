package com.example.leavemanagement.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import com.example.leavemanagement.dto.DashboardStatsResponse;
import com.example.leavemanagement.entity.Employee;
import com.example.leavemanagement.entity.LeaveRequest;
import com.example.leavemanagement.enums.LeaveStatus;
import com.example.leavemanagement.enums.LeaveType;
import com.example.leavemanagement.repository.EmployeeRepository;
import com.example.leavemanagement.repository.LeaveRequestRepository;

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
}
