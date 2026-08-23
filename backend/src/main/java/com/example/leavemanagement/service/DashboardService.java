package com.example.leavemanagement.service;

import com.example.leavemanagement.dto.*;
import com.example.leavemanagement.entity.*;
import com.example.leavemanagement.enums.*;
import com.example.leavemanagement.repository.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    private final EmployeeRepository employeeRepository;
    private final LeaveRequestRepository leaveRequestRepository;

    public DashboardService(EmployeeRepository employeeRepository, LeaveRequestRepository leaveRequestRepository) {
        this.employeeRepository = employeeRepository;
        this.leaveRequestRepository = leaveRequestRepository;
    }

    public DashboardStatsResponse getDashboardStats(Long employeeId) {
        String employeeName = "Guest Employee";
        Employee emp = null;
        if (employeeId != null) {
            emp = employeeRepository.findById(employeeId).orElse(null);
            if (emp != null) {
                employeeName = emp.getFirstName() + " " + emp.getLastName();
            }
        }

        long totalEmployees = employeeRepository.count();
        long pendingApprovals = leaveRequestRepository.countPendingRequests();

        int currentYear = LocalDate.now().getYear();
        LocalDate startOfYear = LocalDate.of(currentYear, 1, 1);
        LocalDate endOfYear = LocalDate.of(currentYear, 12, 31);

        long leaveBookedThisYear = 0;
        if (employeeId != null) {
            leaveBookedThisYear = leaveRequestRepository.sumApprovedDaysForEmployeeAndYear(employeeId, startOfYear, endOfYear);
        } else {
            leaveBookedThisYear = leaveRequestRepository.sumApprovedDaysForYear(startOfYear, endOfYear);
        }

        double clBooked = 0;
        double slBooked = 0;
        double elBooked = 0;

        double clAvailable = 0;
        double slAvailable = 0;
        double elAvailable = 0;

        if (employeeId != null) {
            List<LeaveRequest> requests = leaveRequestRepository.findByEmployeeId(employeeId);
            
            int startYear = currentYear;
            if (emp != null && emp.getCreatedAt() != null) {
                startYear = emp.getCreatedAt().getYear();
            }

            double carriedForwardEL = 0.0;
            for (int yr = startYear; yr < currentYear; yr++) {
                double elEarnedInYr = 12 * 1.25; 
                double elTakenInYr = 0.0;
                for (LeaveRequest req : requests) {
                    if (req.getStatus() == LeaveStatus.APPROVED && req.getLeaveType() == LeaveType.EL) {
                        int reqYear = req.getStartDate().getYear();
                        if (reqYear == yr) {
                            elTakenInYr += req.getNumberOfDays();
                        }
                    }
                }
                carriedForwardEL = Math.max(0, carriedForwardEL + elEarnedInYr - elTakenInYr);
            }

            for (LeaveRequest req : requests) {
                if (req.getStatus() == LeaveStatus.APPROVED) {
                    int reqYear = req.getStartDate().getYear();
                    if (reqYear == currentYear) {
                        if (req.getLeaveType() == LeaveType.CL) {
                            clBooked += req.getNumberOfDays();
                        } else if (req.getLeaveType() == LeaveType.SL) {
                            slBooked += req.getNumberOfDays();
                        } else if (req.getLeaveType() == LeaveType.EL) {
                            elBooked += req.getNumberOfDays();
                        }
                    }
                }
            }

            int currentMonth = LocalDate.now().getMonthValue();
            
            clAvailable = Math.max(0, (currentMonth * 1.0) - clBooked);
            slAvailable = Math.max(0, 5.0 - slBooked);
            elAvailable = Math.max(0, carriedForwardEL + (currentMonth * 1.25) - elBooked);
        }

        return DashboardStatsResponse.builder()
                .employeeName(employeeName)
                .totalEmployees(totalEmployees)
                .pendingApprovals(pendingApprovals)
                .leaveBookedThisYear(leaveBookedThisYear)
                .absentToday(0)
                .casualLeaveAvailable(clAvailable)
                .casualLeaveBooked(clBooked)
                .sickLeaveAvailable(slAvailable)
                .sickLeaveBooked(slBooked)
                .earnedLeaveAvailable(elAvailable)
                .earnedLeaveBooked(elBooked)
                .build();
    }

    public List<TotalLeavesReportResponse> getTotalLeavesReport(int minDays) {
        var employees = employeeRepository.findAll();
        var approvedLeaves = leaveRequestRepository.findAll().stream()
                .filter(r -> r.getStatus() == LeaveStatus.APPROVED)
                .toList();

        var result = new ArrayList<TotalLeavesReportResponse>();
        for (var emp : employees) {
            var empApproved = approvedLeaves.stream()
                    .filter(r -> r.getEmployee().getId().equals(emp.getId()))
                    .toList();
            double totalDays = empApproved.stream().mapToDouble(LeaveRequest::getNumberOfDays).sum();
            if (totalDays >= minDays) {
                var empCode = emp.getEmployeeId() != null ? emp.getEmployeeId() : "EMP" + emp.getId();
                result.add(new TotalLeavesReportResponse(
                        empCode,
                        emp.getFirstName() + " " + emp.getLastName(),
                        totalDays,
                        empApproved.size()
                ));
            }
        }
        return result;
    }

    public List<NoLeavesReportResponse> getNoLeavesReport() {
        var employees = employeeRepository.findAll();
        var allRequests = leaveRequestRepository.findAll();
        var employeesWithLeaves = allRequests.stream()
                .map(r -> r.getEmployee().getId())
                .collect(Collectors.toSet());

        var result = new ArrayList<NoLeavesReportResponse>();
        for (var emp : employees) {
            if (!employeesWithLeaves.contains(emp.getId())) {
                var empCode = emp.getEmployeeId() != null ? emp.getEmployeeId() : "EMP" + emp.getId();
                result.add(new NoLeavesReportResponse(
                        empCode,
                        emp.getFirstName() + " " + emp.getLastName(),
                        emp.getEmail(),
                        emp.getDepartment() != null ? emp.getDepartment() : "-"
                ));
            }
        }
        return result;
    }

    public List<LeaveTypeReportResponse> getFrequentTypeReport() {
        var allRequests = leaveRequestRepository.findAll();

        int clCount = 0;
        double clDays = 0;
        int slCount = 0;
        double slDays = 0;
        int elCount = 0;
        double elDays = 0;

        for (var r : allRequests) {
            switch (r.getLeaveType()) {
                case CL -> {
                    clCount++;
                    clDays += r.getNumberOfDays();
                }
                case SL -> {
                    slCount++;
                    slDays += r.getNumberOfDays();
                }
                case EL -> {
                    elCount++;
                    elDays += r.getNumberOfDays();
                }
            }
        }

        int maxCount = Math.max(clCount, Math.max(slCount, elCount));
        boolean clWinner = clCount == maxCount && maxCount > 0;
        boolean slWinner = slCount == maxCount && maxCount > 0;
        boolean elWinner = elCount == maxCount && maxCount > 0;

        if (maxCount == 0) {
            clWinner = false;
            slWinner = false;
            elWinner = false;
        }

        return Arrays.asList(
                new LeaveTypeReportResponse("CL", "Casual Leave", clCount, clDays, clWinner),
                new LeaveTypeReportResponse("SL", "Sick Leave", slCount, slDays, slWinner),
                new LeaveTypeReportResponse("EL", "Earned Leave", elCount, elDays, elWinner)
        );
    }
}
