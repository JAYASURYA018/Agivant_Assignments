package com.example.leavemanagement.entity;

import com.example.leavemanagement.enums.*;
import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "leave_request")
public class LeaveRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(name = "leave_type", nullable = false, length = 10)
    private LeaveType leaveType;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "number_of_days", nullable = false)
    private Integer numberOfDays;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LeaveStatus status;

    @Column(length = 255)
    private String reason;

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    public LeaveRequest() {}

    public LeaveRequest(Long id, Employee employee, LeaveType leaveType, LocalDate startDate, LocalDate endDate, Integer numberOfDays, LeaveStatus status, String reason, LocalDateTime createdAt) {
        this.id = id;
        this.employee = employee;
        this.leaveType = leaveType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.numberOfDays = numberOfDays;
        this.status = status;
        this.reason = reason;
        this.createdAt = createdAt;
    }

    public Long getId() { 
        return id; 
    }
    
    public void setId(Long id) { 
        this.id = id; 
    }

    public Employee getEmployee() { 
        return employee; 
    }
    
    public void setEmployee(Employee employee) { 
        this.employee = employee; 
    }

    public LeaveType getLeaveType() { 
        return leaveType; 
    }
    
    public void setLeaveType(LeaveType leaveType) { 
        this.leaveType = leaveType; 
    }

    public LocalDate getStartDate() { 
        return startDate; 
    }
    
    public void setStartDate(LocalDate startDate) { 
        this.startDate = startDate; 
    }

    public LocalDate getEndDate() { 
        return endDate; 
    }
    
    public void setEndDate(LocalDate endDate) { 
        this.endDate = endDate; 
    }

    public Integer getNumberOfDays() { 
        return numberOfDays; 
    }
    
    public void setNumberOfDays(Integer numberOfDays) { 
        this.numberOfDays = numberOfDays; 
    }

    public LeaveStatus getStatus() { 
        return status; 
    }
    
    public void setStatus(LeaveStatus status) { 
        this.status = status; 
    }

    public String getReason() { 
        return reason; 
    }
    
    public void setReason(String reason) { 
        this.reason = reason; 
    }

    public LocalDateTime getCreatedAt() { 
        return createdAt; 
    }
    
    public void setCreatedAt(LocalDateTime createdAt) { 
        this.createdAt = createdAt; 
    }
}
