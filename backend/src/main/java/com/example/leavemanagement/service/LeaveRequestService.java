package com.example.leavemanagement.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.leavemanagement.dto.DayLeaveResponse;
import com.example.leavemanagement.dto.EmployeeOnLeaveDto;
import com.example.leavemanagement.dto.LeaveRequestDto;
import com.example.leavemanagement.dto.LeaveResponse;
import com.example.leavemanagement.entity.Employee;
import com.example.leavemanagement.entity.Holiday;
import com.example.leavemanagement.entity.LeaveRequest;
import com.example.leavemanagement.enums.LeaveStatus;
import com.example.leavemanagement.exception.BusinessException;
import com.example.leavemanagement.exception.ResourceNotFoundException;
import com.example.leavemanagement.repository.EmployeeRepository;
import com.example.leavemanagement.repository.HolidayRepository;
import com.example.leavemanagement.repository.LeaveRequestRepository;

@Service
public class LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;
    private final HolidayRepository holidayRepository;

    public LeaveRequestService(LeaveRequestRepository leaveRequestRepository, EmployeeRepository employeeRepository, HolidayRepository holidayRepository) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.employeeRepository = employeeRepository;
        this.holidayRepository = holidayRepository;
    }

    @Transactional
    public LeaveResponse createLeaveRequest(LeaveRequestDto requestDto) {

        Employee employee = employeeRepository.findById(requestDto.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee with ID " + requestDto.getEmployeeId() + " not found."));

        LocalDate startDate = requestDto.getStartDate();
        LocalDate endDate = requestDto.getEndDate();

        if (startDate.isAfter(endDate)) {
            throw new BusinessException("Start date cannot be after end date.");
        }

        List<LeaveRequest> existingRequests = leaveRequestRepository.findByEmployeeId(requestDto.getEmployeeId());
        for (LeaveRequest existing : existingRequests) {
            if (existing.getStatus() == LeaveStatus.APPROVED || existing.getStatus() == LeaveStatus.PENDING) {

                if (!startDate.isAfter(existing.getEndDate()) && !endDate.isBefore(existing.getStartDate())) {
                    throw new BusinessException("Requested leave dates overlap with an existing leave request ("
                            + existing.getStartDate() + " to " + existing.getEndDate() + ", Status: " + existing.getStatus() + ").");
                }
            }
        }

        int calculatedDays = calculateWorkingDays(startDate, endDate);

        LeaveRequest leave = new LeaveRequest();
        leave.setEmployee(employee);
        leave.setLeaveType(requestDto.getLeaveType());
        leave.setStartDate(startDate);
        leave.setEndDate(endDate);
        leave.setNumberOfDays(calculatedDays);
        leave.setStatus(LeaveStatus.PENDING);
        leave.setReason(requestDto.getReason() != null ? requestDto.getReason().trim() : null);

        LeaveRequest saved = leaveRequestRepository.save(leave);
        return LeaveResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<LeaveResponse> getAllLeaveRequests() {
        return leaveRequestRepository.findAll().stream()
                .map(LeaveResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public LeaveResponse getLeaveRequestById(Long id) {
        LeaveRequest req = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request with ID " + id + " not found."));
        return LeaveResponse.fromEntity(req);
    }

    @Transactional
    public LeaveResponse approveLeaveRequest(Long id) {
        LeaveRequest req = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request with ID " + id + " not found."));

        if (req.getStatus() == LeaveStatus.CANCELLED) {
            throw new BusinessException("Cannot approve a cancelled leave request.");
        }
        if (req.getStatus() == LeaveStatus.APPROVED) {
            return LeaveResponse.fromEntity(req);
        }

        req.setStatus(LeaveStatus.APPROVED);
        LeaveRequest saved = leaveRequestRepository.save(req);
        return LeaveResponse.fromEntity(saved);
    }

    @Transactional
    public LeaveResponse rejectLeaveRequest(Long id) {
        LeaveRequest req = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request with ID " + id + " not found."));

        if (req.getStatus() == LeaveStatus.APPROVED) {
            throw new BusinessException("An approved leave request cannot be rejected.");
        }
        if (req.getStatus() == LeaveStatus.CANCELLED) {
            throw new BusinessException("Cannot reject a cancelled leave request.");
        }

        if (req.getStatus() == LeaveStatus.REJECTED) {
            return LeaveResponse.fromEntity(req);
        }

        req.setStatus(LeaveStatus.REJECTED);
        LeaveRequest saved = leaveRequestRepository.save(req);
        return LeaveResponse.fromEntity(saved);
    }

    @Transactional
    public LeaveResponse cancelLeaveRequest(Long id) {
        LeaveRequest req = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request with ID " + id + " not found."));

        if (req.getStatus() == LeaveStatus.REJECTED) {
            throw new BusinessException("A rejected leave request cannot be cancelled.");
        }
        if (req.getStatus() == LeaveStatus.CANCELLED) {
            return LeaveResponse.fromEntity(req);
        }

        req.setStatus(LeaveStatus.CANCELLED);
        LeaveRequest saved = leaveRequestRepository.save(req);
        return LeaveResponse.fromEntity(saved);
    }

    public int calculateWorkingDays(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            return 0;
        }

        List<Holiday> holidays = holidayRepository.findAll();
        Set<LocalDate> holidayDates = holidays.stream()
                .map(Holiday::getHolidayDate)
                .collect(Collectors.toSet());

        int workingDays = 0;
        LocalDate date = startDate;
        while (!date.isAfter(endDate)) {
            DayOfWeek day = date.getDayOfWeek();
            if (day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY) {
                if (!holidayDates.contains(date)) {
                    workingDays++;
                }
            }
            date = date.plusDays(1);
        }
        return workingDays;
    }

    @Transactional(readOnly = true)
    public List<DayLeaveResponse> getLeavesOnDays(LocalDate startDate, LocalDate endDate) {
        List<LeaveRequest> allLeaves = leaveRequestRepository.findApprovedLeavesInRange(startDate, endDate);
        List<DayLeaveResponse> result = new ArrayList<>();
        LocalDate cur = startDate;
        while (!cur.isAfter(endDate)) {
            final LocalDate currentDate = cur;
            List<LeaveRequest> activeLeaves = allLeaves.stream()
                    .filter(req -> !currentDate.isBefore(req.getStartDate()) && !currentDate.isAfter(req.getEndDate()))
                    .collect(Collectors.toList());

            List<EmployeeOnLeaveDto> emps = activeLeaves.stream()
                    .map(req -> new EmployeeOnLeaveDto(
                    req.getEmployee().getId(),
                    req.getEmployee().getFirstName() + " " + req.getEmployee().getLastName(),
                    req.getEmployee().getDepartment(),
                    req.getLeaveType().toString(),
                    req.getReason()
            ))
                    .collect(Collectors.toList());

            result.add(new DayLeaveResponse(currentDate, emps.size(), emps));
            cur = cur.plusDays(1);
        }
        return result;
    }
}
