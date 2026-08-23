package com.example.leavemanagement.service;

import com.example.leavemanagement.entity.Holiday;
import com.example.leavemanagement.repository.HolidayRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class HolidayService {

    private final HolidayRepository holidayRepository;

    public HolidayService(HolidayRepository holidayRepository) {
        this.holidayRepository = holidayRepository;
    }

    public List<Holiday> getUpcomingHolidays() {
        
        return holidayRepository.findUpcomingHolidays(LocalDate.now());
    }

    public List<Holiday> getAllHolidays() {
        return holidayRepository.findAllByOrderByHolidayDateAsc();
    }
}
