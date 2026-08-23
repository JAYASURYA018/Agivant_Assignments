package com.example.leavemanagement.controller;

import com.example.leavemanagement.dto.*;
import com.example.leavemanagement.service.LeaveRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leaves")
@Tag(name = "Leave Requests", description = "Endpoints for creating and managing employee leave requests")
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    public LeaveRequestController(LeaveRequestService leaveRequestService) {
        this.leaveRequestService = leaveRequestService;
    }

    @PostMapping
    @Operation(summary = "Submit a new leave request")
    public ResponseEntity<LeaveResponse> createLeaveRequest(@Valid @RequestBody LeaveRequestDto request) {
        LeaveResponse created = leaveRequestService.createLeaveRequest(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get list of all leave requests")
    public ResponseEntity<List<LeaveResponse>> getAllLeaveRequests() {
        List<LeaveResponse> requests = leaveRequestService.getAllLeaveRequests();
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a leave request by ID")
    public ResponseEntity<LeaveResponse> getLeaveRequestById(@PathVariable("id") Long id) {
        LeaveResponse request = leaveRequestService.getLeaveRequestById(id);
        return ResponseEntity.ok(request);
    }

    @PutMapping("/{id}/approve")
    @Operation(summary = "Approve a leave request")
    public ResponseEntity<LeaveResponse> approveLeaveRequest(@PathVariable("id") Long id) {
        LeaveResponse approved = leaveRequestService.approveLeaveRequest(id);
        return ResponseEntity.ok(approved);
    }

    @PutMapping("/{id}/reject")
    @Operation(summary = "Reject a leave request")
    public ResponseEntity<LeaveResponse> rejectLeaveRequest(@PathVariable("id") Long id) {
        LeaveResponse rejected = leaveRequestService.rejectLeaveRequest(id);
        return ResponseEntity.ok(rejected);
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel a leave request")
    public ResponseEntity<LeaveResponse> cancelLeaveRequest(@PathVariable("id") Long id) {
        LeaveResponse cancelled = leaveRequestService.cancelLeaveRequest(id);
        return ResponseEntity.ok(cancelled);
    }

    @GetMapping("/on-leave")
    @Operation(summary = "Get daily list of leaves and employees on leave for a date range")
    public ResponseEntity<List<DayLeaveResponse>> getLeavesOnDays(
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        List<DayLeaveResponse> leaves = leaveRequestService.getLeavesOnDays(start, end);
        return ResponseEntity.ok(leaves);
    }

    @GetMapping("/calculate-days")
    @Operation(summary = "Calculate working days excluding weekends and public holidays")
    public ResponseEntity<Integer> calculateWorkingDays(
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        if (start.isAfter(end)) {
            throw new com.example.leavemanagement.exception.BusinessException("Start date cannot be after end date.");
        }
        int days = leaveRequestService.calculateWorkingDays(start, end);
        return ResponseEntity.ok(days);
    }
}
