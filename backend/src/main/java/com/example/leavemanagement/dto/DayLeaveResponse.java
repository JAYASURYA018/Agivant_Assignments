package com.example.leavemanagement.dto;

import java.time.LocalDate;
import java.util.List;

public class DayLeaveResponse {
    private LocalDate date;
    private int leaveCount;
    private List<EmployeeOnLeaveDto> employees;

    public DayLeaveResponse() {}

    public DayLeaveResponse(LocalDate date, int leaveCount, List<EmployeeOnLeaveDto> employees) {
        this.date = date;
        this.leaveCount = leaveCount;
        this.employees = employees;
    }

    
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public int getLeaveCount() { return leaveCount; }
    public void setLeaveCount(int leaveCount) { this.leaveCount = leaveCount; }

    public List<EmployeeOnLeaveDto> getEmployees() { return employees; }
    public void setEmployees(List<EmployeeOnLeaveDto> employees) { this.employees = employees; }
}
