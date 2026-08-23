package com.example.leavemanagement.service;

import com.example.leavemanagement.dto.*;
import com.example.leavemanagement.entity.Employee;
import com.example.leavemanagement.exception.*;
import com.example.leavemanagement.repository.EmployeeRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Transactional
    public EmployeeResponse createEmployee(EmployeeRequest request) {
        
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new BusinessException("Password is required.");
        }
        if (request.getPassword().length() < 6 || request.getPassword().length() > 100) {
            throw new BusinessException("Password must be between 6 and 100 characters.");
        }

        
        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("An employee with email '" + request.getEmail() + "' already exists.");
        }

        Employee emp = new Employee();

        
        if (request.getEmployeeId() != null && !request.getEmployeeId().trim().isEmpty()) {
            emp.setEmployeeId(request.getEmployeeId().trim());
        } else {
            long count = employeeRepository.count();
            long suffix = 1000 + count + 1;
            String candidateId = "Z" + suffix;
            while (employeeRepository.existsByEmployeeId(candidateId)) {
                suffix++;
                candidateId = "Z" + suffix;
            }
            emp.setEmployeeId(candidateId);
        }

        emp.setFirstName(request.getFirstName().trim());
        emp.setLastName(request.getLastName().trim());
        emp.setEmail(request.getEmail().trim().toLowerCase());
        emp.setPassword(com.example.leavemanagement.util.PasswordUtil.hashPassword(request.getPassword()));
        
        if (request.getDepartment() != null) {
            emp.setDepartment(request.getDepartment().trim());
        }

        if (request.getRole() != null && !request.getRole().trim().isEmpty()) {
            emp.setRole(request.getRole().trim().toUpperCase());
        } else {
            emp.setRole("EMPLOYEE");
        }

        Employee saved = employeeRepository.save(emp);
        return EmployeeResponse.fromEntity(saved);
    }

    @Transactional
    public EmployeeResponse updateEmployee(Long id, EmployeeRequest request) {
        Employee emp = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee with ID " + id + " not found."));

        
        String newEmail = request.getEmail().trim().toLowerCase();
        if (!emp.getEmail().equals(newEmail)) {
            if (employeeRepository.existsByEmail(newEmail)) {
                throw new BusinessException("An employee with email '" + newEmail + "' already exists.");
            }
            emp.setEmail(newEmail);
        }

        emp.setFirstName(request.getFirstName().trim());
        emp.setLastName(request.getLastName().trim());
        
        if (request.getDepartment() != null) {
            emp.setDepartment(request.getDepartment().trim());
        }

        if (request.getRole() != null && !request.getRole().trim().isEmpty()) {
            emp.setRole(request.getRole().trim().toUpperCase());
        }

        
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            if (request.getPassword().length() < 6 || request.getPassword().length() > 100) {
                throw new BusinessException("Password must be between 6 and 100 characters.");
            }
            emp.setPassword(com.example.leavemanagement.util.PasswordUtil.hashPassword(request.getPassword()));
        }

        Employee saved = employeeRepository.save(emp);
        return EmployeeResponse.fromEntity(saved);
    }

    @Transactional
    public void deleteEmployee(Long id) {
        if (!employeeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Employee with ID " + id + " not found.");
        }
        employeeRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<EmployeeResponse> getAllEmployees() {
        return employeeRepository.findAll().stream()
                .map(EmployeeResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EmployeeResponse> getAllEmployees(String sortBy, String direction) {
        org.springframework.data.domain.Sort sort = "desc".equalsIgnoreCase(direction)
                ? org.springframework.data.domain.Sort.by(sortBy).descending()
                : org.springframework.data.domain.Sort.by(sortBy).ascending();
        
        
        if ("firstName".equals(sortBy)) {
            sort = "desc".equalsIgnoreCase(direction)
                    ? org.springframework.data.domain.Sort.by("firstName").descending().and(org.springframework.data.domain.Sort.by("lastName").descending())
                    : org.springframework.data.domain.Sort.by("firstName").ascending().and(org.springframework.data.domain.Sort.by("lastName").ascending());
        }

        return employeeRepository.findAll(sort).stream()
                .map(EmployeeResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(Long id) {
        Employee emp = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee with ID " + id + " not found."));
        return EmployeeResponse.fromEntity(emp);
    }

    @Transactional(readOnly = true)
    public EmployeeResponse login(LoginRequest request) {
        Employee emp = employeeRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new BusinessException("Invalid email or password."));

        if (!com.example.leavemanagement.util.PasswordUtil.checkPassword(request.getPassword(), emp.getPassword())) {
            throw new BusinessException("Invalid email or password.");
        }

        return EmployeeResponse.fromEntity(emp);
    }
}
