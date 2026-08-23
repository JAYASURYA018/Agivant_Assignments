package com.example.leavemanagement.controller;

import com.example.leavemanagement.dto.*;
import com.example.leavemanagement.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Endpoints for employee login and session registration")
public class AuthController {

    private final EmployeeService employeeService;

    public AuthController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate employee with email and password")
    public ResponseEntity<EmployeeResponse> login(@Valid @RequestBody LoginRequest request) {
        EmployeeResponse response = employeeService.login(request);
        return ResponseEntity.ok(response);
    }
}
