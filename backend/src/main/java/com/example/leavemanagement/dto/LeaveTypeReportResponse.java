package com.example.leavemanagement.dto;

public class LeaveTypeReportResponse {
    private String leaveType;
    private String label;
    private int count;
    private double days;
    private boolean isWinner;

    public LeaveTypeReportResponse(String leaveType, String label, int count, double days, boolean isWinner) {
        this.leaveType = leaveType;
        this.label = label;
        this.count = count;
        this.days = days;
        this.isWinner = isWinner;
    }

    public String getLeaveType() { return leaveType; }
    public void setLeaveType(String leaveType) { this.leaveType = leaveType; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }

    public double getDays() { return days; }
    public void setDays(double days) { this.days = days; }

    public boolean getIsWinner() { return isWinner; }
    public void setIsWinner(boolean isWinner) { this.isWinner = isWinner; }
}
