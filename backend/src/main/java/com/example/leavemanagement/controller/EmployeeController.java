package com.example.leavemanagement.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.leavemanagement.dto.EmployeeRequest;
import com.example.leavemanagement.dto.EmployeeResponse;
import com.example.leavemanagement.service.EmployeeService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping("/createEmp")
    public ResponseEntity<EmployeeResponse> createEmployee(@Valid @RequestBody EmployeeRequest request) {
        EmployeeResponse created = employeeService.createEmployee(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<EmployeeResponse>> getAllEmployees(
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "direction", required = false) String direction) {

        if (sortBy == null || sortBy.trim().isEmpty()) {
            List<EmployeeResponse> employees = employeeService.getAllEmployees();
            return ResponseEntity.ok(employees);
        }

        String dbField = switch (sortBy) {
            case "id" ->
                "employeeId";
            case "name" ->
                "firstName";
            case "email" ->
                "email";
            case "department" ->
                "department";
            case "role" ->
                "role";
            default ->
                "employeeId";
        };

        String dir = (direction != null) ? direction : "asc";
        List<EmployeeResponse> employees = employeeService.getAllEmployees(dbField, dir);
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponse> getEmployeeById(@PathVariable("id") Long id) {
        EmployeeResponse employee = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(employee);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponse> updateEmployee(@PathVariable("id") Long id, @Valid @RequestBody EmployeeRequest request) {
        EmployeeResponse updated = employeeService.updateEmployee(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable("id") Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
}
