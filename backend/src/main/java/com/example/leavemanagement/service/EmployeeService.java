package com.example.leavemanagement.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.leavemanagement.config.JwtTokenProvider;
import com.example.leavemanagement.dto.EmployeeRequest;
import com.example.leavemanagement.dto.EmployeeResponse;
import com.example.leavemanagement.dto.LoginRequest;
import com.example.leavemanagement.entity.Employee;
import com.example.leavemanagement.exception.BusinessException;
import com.example.leavemanagement.exception.ResourceNotFoundException;
import com.example.leavemanagement.repository.EmployeeRepository;
import com.example.leavemanagement.util.PasswordUtil;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public EmployeeService(EmployeeRepository employeeRepository, JwtTokenProvider jwtTokenProvider) {
        this.employeeRepository = employeeRepository;
        this.jwtTokenProvider = jwtTokenProvider;
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
        emp.setPassword(PasswordUtil.hashPassword(request.getPassword()));

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
            emp.setPassword(PasswordUtil.hashPassword(request.getPassword()));
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
        Sort sort = "desc".equalsIgnoreCase(direction)
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        if ("firstName".equals(sortBy)) {
            sort = "desc".equalsIgnoreCase(direction)
                    ? Sort.by("firstName").descending().and(Sort.by("lastName").descending())
                    : Sort.by("firstName").ascending().and(Sort.by("lastName").ascending());
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

        if (!PasswordUtil.checkPassword(request.getPassword(), emp.getPassword())) {
            throw new BusinessException("Invalid email or password.");
        }

        EmployeeResponse response = EmployeeResponse.fromEntity(emp);
        String token = jwtTokenProvider.generateToken(emp.getId(), emp.getEmail(), emp.getRole());
        response.setToken(token);
        return response;
    }
}
