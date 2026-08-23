package com.example.leavemanagement.controller;

import com.example.leavemanagement.dto.*;
import com.example.leavemanagement.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "Endpoints for retrieving system metrics and statistics")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/stats")
    @Operation(summary = "Get metrics for the home dashboard")
    public ResponseEntity<DashboardStatsResponse> getDashboardStats(
            @RequestParam(value = "employeeId", required = false, defaultValue = "1") Long employeeId) {
        DashboardStatsResponse stats = dashboardService.getDashboardStats(employeeId);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/reports/total-leaves")
    @Operation(summary = "Get Total Leaves approved report summary per employee")
    public ResponseEntity<List<TotalLeavesReportResponse>> getTotalLeavesReport(
            @RequestParam(value = "minDays", required = false, defaultValue = "0") int minDays) {
        List<TotalLeavesReportResponse> report = dashboardService.getTotalLeavesReport(minDays);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/reports/no-leaves")
    @Operation(summary = "Get list of employees who have never filed a leave request")
    public ResponseEntity<List<NoLeavesReportResponse>> getNoLeavesReport() {
        List<NoLeavesReportResponse> report = dashboardService.getNoLeavesReport();
        return ResponseEntity.ok(report);
    }

    @GetMapping("/reports/frequent-type")
    @Operation(summary = "Get aggregated statistics of leave counts per category")
    public ResponseEntity<List<LeaveTypeReportResponse>> getFrequentTypeReport() {
        List<LeaveTypeReportResponse> report = dashboardService.getFrequentTypeReport();
        return ResponseEntity.ok(report);
    }
}
