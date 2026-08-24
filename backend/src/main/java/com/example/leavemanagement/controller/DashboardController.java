package com.example.leavemanagement.controller;

import com.example.leavemanagement.dto.*;
import com.example.leavemanagement.service.DashboardService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsResponse> getDashboardStats(
            @RequestParam(value = "employeeId", required = false, defaultValue = "1") Long employeeId) {
        DashboardStatsResponse stats = dashboardService.getDashboardStats(employeeId);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/reports/total-leaves")
    public ResponseEntity<List<TotalLeavesReportResponse>> getTotalLeavesReport(
            @RequestParam(value = "minDays", required = false, defaultValue = "0") int minDays) {
        List<TotalLeavesReportResponse> report = dashboardService.getTotalLeavesReport(minDays);
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
