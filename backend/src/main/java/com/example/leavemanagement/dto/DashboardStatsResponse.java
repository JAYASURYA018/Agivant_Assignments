package com.example.leavemanagement.dto;

public class DashboardStatsResponse {

    private long totalEmployees;
    private long pendingApprovals;
    private long leaveBookedThisYear;
    private long absentToday;

    private double casualLeaveAvailable;
    private double casualLeaveBooked;
    private double sickLeaveAvailable;
    private double sickLeaveBooked;
    private double earnedLeaveAvailable;
    private double earnedLeaveBooked;

    public DashboardStatsResponse() {
    }

    public DashboardStatsResponse(long totalEmployees, long pendingApprovals, long leaveBookedThisYear, long absentToday,
            double casualLeaveAvailable, double casualLeaveBooked,
            double sickLeaveAvailable, double sickLeaveBooked,
            double earnedLeaveAvailable, double earnedLeaveBooked) {

        this.totalEmployees = totalEmployees;
        this.pendingApprovals = pendingApprovals;
        this.leaveBookedThisYear = leaveBookedThisYear;
        this.absentToday = absentToday;
        this.casualLeaveAvailable = casualLeaveAvailable;
        this.casualLeaveBooked = casualLeaveBooked;
        this.sickLeaveAvailable = sickLeaveAvailable;
        this.sickLeaveBooked = sickLeaveBooked;
        this.earnedLeaveAvailable = earnedLeaveAvailable;
        this.earnedLeaveBooked = earnedLeaveBooked;
    }

    public long getTotalEmployees() {
        return totalEmployees;
    }

    public void setTotalEmployees(long totalEmployees) {
        this.totalEmployees = totalEmployees;
    }

    public long getPendingApprovals() {
        return pendingApprovals;
    }

    public void setPendingApprovals(long pendingApprovals) {
        this.pendingApprovals = pendingApprovals;
    }

    public long getLeaveBookedThisYear() {
        return leaveBookedThisYear;
    }

    public void setLeaveBookedThisYear(long leaveBookedThisYear) {
        this.leaveBookedThisYear = leaveBookedThisYear;
    }

    public long getAbsentToday() {
        return absentToday;
    }

    public void setAbsentToday(long absentToday) {
        this.absentToday = absentToday;
    }

    public double getCasualLeaveAvailable() {
        return casualLeaveAvailable;
    }

    public void setCasualLeaveAvailable(double casualLeaveAvailable) {
        this.casualLeaveAvailable = casualLeaveAvailable;
    }

    public double getCasualLeaveBooked() {
        return casualLeaveBooked;
    }

    public void setCasualLeaveBooked(double casualLeaveBooked) {
        this.casualLeaveBooked = casualLeaveBooked;
    }

    public double getSickLeaveAvailable() {
        return sickLeaveAvailable;
    }

    public void setSickLeaveAvailable(double sickLeaveAvailable) {
        this.sickLeaveAvailable = sickLeaveAvailable;
    }

    public double getSickLeaveBooked() {
        return sickLeaveBooked;
    }

    public void setSickLeaveBooked(double sickLeaveBooked) {
        this.sickLeaveBooked = sickLeaveBooked;
    }

    public double getEarnedLeaveAvailable() {
        return earnedLeaveAvailable;
    }

    public void setEarnedLeaveAvailable(double earnedLeaveAvailable) {
        this.earnedLeaveAvailable = earnedLeaveAvailable;
    }

    public double getEarnedLeaveBooked() {
        return earnedLeaveBooked;
    }

    public void setEarnedLeaveBooked(double earnedLeaveBooked) {
        this.earnedLeaveBooked = earnedLeaveBooked;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private long totalEmployees;
        private long pendingApprovals;
        private long leaveBookedThisYear;
        private long absentToday;
        private double casualLeaveAvailable;
        private double casualLeaveBooked;
        private double sickLeaveAvailable;
        private double sickLeaveBooked;
        private double earnedLeaveAvailable;
        private double earnedLeaveBooked;

        public Builder totalEmployees(long totalEmployees) {
            this.totalEmployees = totalEmployees;
            return this;
        }

        public Builder pendingApprovals(long pendingApprovals) {
            this.pendingApprovals = pendingApprovals;
            return this;
        }

        public Builder leaveBookedThisYear(long leaveBookedThisYear) {
            this.leaveBookedThisYear = leaveBookedThisYear;
            return this;
        }

        public Builder absentToday(long absentToday) {
            this.absentToday = absentToday;
            return this;
        }

        public Builder casualLeaveAvailable(double casualLeaveAvailable) {
            this.casualLeaveAvailable = casualLeaveAvailable;
            return this;
        }

        public Builder casualLeaveBooked(double casualLeaveBooked) {
            this.casualLeaveBooked = casualLeaveBooked;
            return this;
        }

        public Builder sickLeaveAvailable(double sickLeaveAvailable) {
            this.sickLeaveAvailable = sickLeaveAvailable;
            return this;
        }

        public Builder sickLeaveBooked(double sickLeaveBooked) {
            this.sickLeaveBooked = sickLeaveBooked;
            return this;
        }

        public Builder earnedLeaveAvailable(double earnedLeaveAvailable) {
            this.earnedLeaveAvailable = earnedLeaveAvailable;
            return this;
        }

        public Builder earnedLeaveBooked(double earnedLeaveBooked) {
            this.earnedLeaveBooked = earnedLeaveBooked;
            return this;
        }

        public DashboardStatsResponse build() {
            return new DashboardStatsResponse(
                    totalEmployees, pendingApprovals, leaveBookedThisYear, absentToday,
                    casualLeaveAvailable, casualLeaveBooked,
                    sickLeaveAvailable, sickLeaveBooked,
                    earnedLeaveAvailable, earnedLeaveBooked
            );
        }
    }
}
