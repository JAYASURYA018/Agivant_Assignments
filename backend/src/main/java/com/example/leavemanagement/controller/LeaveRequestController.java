package com.example.leavemanagement.controller;

import com.example.leavemanagement.dto.*;
import com.example.leavemanagement.service.LeaveRequestService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leaves")
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    public LeaveRequestController(LeaveRequestService leaveRequestService) {
        this.leaveRequestService = leaveRequestService;
    }

    @PostMapping
    public ResponseEntity<LeaveResponse> createLeaveRequest(@Valid @RequestBody LeaveRequestDto request) {
        LeaveResponse created = leaveRequestService.createLeaveRequest(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<LeaveResponse>> getAllLeaveRequests() {
        List<LeaveResponse> requests = leaveRequestService.getAllLeaveRequests();
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeaveResponse> getLeaveRequestById(@PathVariable("id") Long id) {
        LeaveResponse request = leaveRequestService.getLeaveRequestById(id);
        return ResponseEntity.ok(request);
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<LeaveResponse> approveLeaveRequest(@PathVariable("id") Long id) {
        LeaveResponse approved = leaveRequestService.approveLeaveRequest(id);
        return ResponseEntity.ok(approved);
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<LeaveResponse> rejectLeaveRequest(@PathVariable("id") Long id) {
        LeaveResponse rejected = leaveRequestService.rejectLeaveRequest(id);
        return ResponseEntity.ok(rejected);
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<LeaveResponse> cancelLeaveRequest(@PathVariable("id") Long id) {
        LeaveResponse cancelled = leaveRequestService.cancelLeaveRequest(id);
        return ResponseEntity.ok(cancelled);
    }

    @GetMapping("/on-leave")
    public ResponseEntity<List<DayLeaveResponse>> getLeavesOnDays(
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        List<DayLeaveResponse> leaves = leaveRequestService.getLeavesOnDays(start, end);
        return ResponseEntity.ok(leaves);
    }

    @GetMapping("/calculate-days")
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
