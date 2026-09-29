package com.example.leavemanagement.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.leavemanagement.entity.Employee;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    boolean existsByEmail(String email);

    boolean existsByEmployeeId(String employeeId);

    Optional<Employee> findByEmail(String email);
}
