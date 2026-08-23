package com.example.leavemanagement.dto;

public class EmployeeOnLeaveDto {
    private Long id;
    private String name;
    private String department;
    private String leaveType;
    private String reason;

    public EmployeeOnLeaveDto() {}

    public EmployeeOnLeaveDto(Long id, String name, String department, String leaveType, String reason) {
        this.id = id;
        this.name = name;
        this.department = department;
        this.leaveType = leaveType;
        this.reason = reason;
    }

    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getLeaveType() { return leaveType; }
    public void setLeaveType(String leaveType) { this.leaveType = leaveType; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
