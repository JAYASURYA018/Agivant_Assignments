package com.example.leavemanagement.dto;

import com.example.leavemanagement.entity.LeaveRequest;
import com.example.leavemanagement.enums.*;
import java.time.*;

public class LeaveResponse {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private LeaveType leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer numberOfDays;
    private LeaveStatus status;
    private String reason;
    private LocalDateTime createdAt;

    public LeaveResponse() {}

    public LeaveResponse(Long id, Long employeeId, String employeeName, LeaveType leaveType, 
                         LocalDate startDate, LocalDate endDate, Integer numberOfDays, 
                         LeaveStatus status, String reason, LocalDateTime createdAt) {
        this.id = id;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.leaveType = leaveType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.numberOfDays = numberOfDays;
        this.status = status;
        this.reason = reason;
        this.createdAt = createdAt;
    }

    public static LeaveResponse fromEntity(LeaveRequest req) {
        String empName = req.getEmployee() != null 
            ? req.getEmployee().getFirstName() + " " + req.getEmployee().getLastName() 
            : "Unknown";
        Long empId = req.getEmployee() != null ? req.getEmployee().getId() : null;

        return new LeaveResponse(
            req.getId(),
            empId,
            empName,
            req.getLeaveType(),
            req.getStartDate(),
            req.getEndDate(),
            req.getNumberOfDays(),
            req.getStatus(),
            req.getReason(),
            req.getCreatedAt()
        );
    }

    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public LeaveType getLeaveType() { return leaveType; }
    public void setLeaveType(LeaveType leaveType) { this.leaveType = leaveType; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public Integer getNumberOfDays() { return numberOfDays; }
    public void setNumberOfDays(Integer numberOfDays) { this.numberOfDays = numberOfDays; }

    public LeaveStatus getStatus() { return status; }
    public void setStatus(LeaveStatus status) { this.status = status; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
