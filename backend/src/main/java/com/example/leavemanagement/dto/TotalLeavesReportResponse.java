package com.example.leavemanagement.dto;

public class TotalLeavesReportResponse {
    private String employeeId;
    private String name;
    private double totalDays;
    private int requestsCount;

    public TotalLeavesReportResponse(String employeeId, String name, double totalDays, int requestsCount) {
        this.employeeId = employeeId;
        this.name = name;
        this.totalDays = totalDays;
        this.requestsCount = requestsCount;
    }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getTotalDays() { return totalDays; }
    public void setTotalDays(double totalDays) { this.totalDays = totalDays; }

    public int getRequestsCount() { return requestsCount; }
    public void setRequestsCount(int requestsCount) { this.requestsCount = requestsCount; }
}
