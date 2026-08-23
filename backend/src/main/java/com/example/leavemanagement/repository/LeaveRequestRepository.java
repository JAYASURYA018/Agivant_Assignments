package com.example.leavemanagement.repository;

import com.example.leavemanagement.entity.LeaveRequest;
import com.example.leavemanagement.enums.LeaveStatus;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findByEmployeeId(Long employeeId);

    List<LeaveRequest> findByStatus(LeaveStatus status);

    @Query("SELECT COUNT(lr) FROM LeaveRequest lr WHERE lr.status = 'PENDING'")
    long countPendingRequests();

    @Query("SELECT COALESCE(SUM(lr.numberOfDays), 0) FROM LeaveRequest lr WHERE lr.status = 'APPROVED' AND lr.startDate >= :yearStart AND lr.endDate <= :yearEnd")
    long sumApprovedDaysForYear(@Param("yearStart") LocalDate yearStart, @Param("yearEnd") LocalDate yearEnd);

    @Query("SELECT COALESCE(SUM(lr.numberOfDays), 0) FROM LeaveRequest lr WHERE lr.employee.id = :employeeId AND lr.status = 'APPROVED' AND lr.startDate >= :yearStart AND lr.endDate <= :yearEnd")
    long sumApprovedDaysForEmployeeAndYear(@Param("employeeId") Long employeeId, @Param("yearStart") LocalDate yearStart, @Param("yearEnd") LocalDate yearEnd);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.status = com.example.leavemanagement.enums.LeaveStatus.APPROVED AND lr.startDate <= :date AND lr.endDate >= :date")
    List<LeaveRequest> findApprovedLeavesOnDate(@Param("date") LocalDate date);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.status = com.example.leavemanagement.enums.LeaveStatus.APPROVED AND lr.startDate <= :endDate AND lr.endDate >= :startDate")
    List<LeaveRequest> findApprovedLeavesInRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
