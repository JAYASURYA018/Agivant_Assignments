package com.example.leavemanagement.repository;

import com.example.leavemanagement.entity.Employee;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    boolean existsByEmail(String email);
    boolean existsByEmployeeId(String employeeId);
    Optional<Employee> findByEmail(String email);
}
