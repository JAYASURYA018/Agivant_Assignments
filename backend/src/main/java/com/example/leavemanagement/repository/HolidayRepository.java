package com.example.leavemanagement.repository;

import com.example.leavemanagement.entity.Holiday;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface HolidayRepository extends JpaRepository<Holiday, Long> {

    @Query("SELECT h FROM Holiday h WHERE h.holidayDate >= :startDate ORDER BY h.holidayDate ASC")
    List<Holiday> findUpcomingHolidays(@Param("startDate") LocalDate startDate);

    boolean existsByHolidayDate(LocalDate holidayDate);

    List<Holiday> findAllByOrderByHolidayDateAsc();
}
