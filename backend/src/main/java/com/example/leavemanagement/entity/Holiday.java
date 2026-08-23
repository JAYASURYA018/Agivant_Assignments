package com.example.leavemanagement.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "holiday")
public class Holiday {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "holiday_date", nullable = false, unique = true)
    private LocalDate holidayDate;

    @Column(length = 255)
    private String description;

    @Column(name = "holiday_type", length = 50)
    private String holidayType;

    public Holiday() {}

    public Holiday(Long id, String name, LocalDate holidayDate, String description, String holidayType) {
        this.id = id;
        this.name = name;
        this.holidayDate = holidayDate;
        this.description = description;
        this.holidayType = holidayType;
    }

    public Long getId() { 
        return id; 
    }
    
    public void setId(Long id) { 
        this.id = id; 
    }

    public String getName() { 
        return name; 
    }
    
    public void setName(String name) { 
        this.name = name; 
    }

    public LocalDate getHolidayDate() { 
        return holidayDate; 
    }
    
    public void setHolidayDate(LocalDate holidayDate) { 
        this.holidayDate = holidayDate; 
    }

    public String getDescription() { 
        return description; 
    }
    
    public void setDescription(String description) { 
        this.description = description; 
    }

    public String getHolidayType() { 
        return holidayType; 
    }
    
    public void setHolidayType(String holidayType) { 
        this.holidayType = holidayType; 
    }
}
