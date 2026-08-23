package com.example.leavemanagement.dto;

import com.example.leavemanagement.entity.Employee;
import java.time.LocalDateTime;

public class EmployeeResponse {
    private Long id;
    private String employeeId;
    private String firstName;
    private String lastName;
    private String email;
    private String department;
    private String role;
    private LocalDateTime createdAt;
    private String token;

    public EmployeeResponse() {}

    public EmployeeResponse(Long id, String employeeId, String firstName, String lastName, String email, String department, String role, LocalDateTime createdAt) {
        this.id = id;
        this.employeeId = employeeId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.department = department;
        this.role = role;
        this.createdAt = createdAt;
    }

    public static EmployeeResponse fromEntity(Employee emp) {
        return new EmployeeResponse(
            emp.getId(),
            emp.getEmployeeId(),
            emp.getFirstName(),
            emp.getLastName(),
            emp.getEmail(),
            emp.getDepartment(),
            emp.getRole(),
            emp.getCreatedAt()
        );
    }

    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}
