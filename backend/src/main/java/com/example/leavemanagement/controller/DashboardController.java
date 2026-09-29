package com.example.leavemanagement.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.leavemanagement.dto.DashboardStatsResponse;
import com.example.leavemanagement.dto.LeaveTypeReportResponse;
import com.example.leavemanagement.dto.NoLeavesReportResponse;
import com.example.leavemanagement.dto.TotalLeavesReportResponse;
import com.example.leavemanagement.service.DashboardService;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsResponse> getDashboardStats(
            @RequestParam(value = "employeeId", required = false) Long employeeId) {
        DashboardStatsResponse stats = dashboardService.getDashboardStats(employeeId);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/reports/total-leaves")
    public ResponseEntity<List<TotalLeavesReportResponse>> getTotalLeavesReport(
            @RequestParam(value = "minDays", required = false) Integer minDays) {
        int daysLimit = minDays != null ? minDays : 0;
        List<TotalLeavesReportResponse> report = dashboardService.getTotalLeavesReport(daysLimit);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/reports/no-leaves")
    public ResponseEntity<List<NoLeavesReportResponse>> getNoLeavesReport() {
        List<NoLeavesReportResponse> report = dashboardService.getNoLeavesReport();
        return ResponseEntity.ok(report);
    }

    @GetMapping("/reports/frequent-type")
    public ResponseEntity<List<LeaveTypeReportResponse>> getFrequentTypeReport() {
        List<LeaveTypeReportResponse> report = dashboardService.getFrequentTypeReport();
        return ResponseEntity.ok(report);
    }
}
