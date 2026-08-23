package com.example.leavemanagement.controller;

import com.example.leavemanagement.entity.Holiday;
import com.example.leavemanagement.service.HolidayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/holidays")
@Tag(name = "Holidays", description = "Endpoints for managing and listing public holidays")
public class HolidayController {

    private final HolidayService holidayService;

    public HolidayController(HolidayService holidayService) {
        this.holidayService = holidayService;
    }

    @GetMapping
    @Operation(summary = "Get all holidays")
    public ResponseEntity<List<Holiday>> getAllHolidays() {
        return ResponseEntity.ok(holidayService.getAllHolidays());
    }

    @GetMapping("/upcoming")
    @Operation(summary = "Get upcoming holidays starting from today")
    public ResponseEntity<List<Holiday>> getUpcomingHolidays() {
        return ResponseEntity.ok(holidayService.getUpcomingHolidays());
    }
}
