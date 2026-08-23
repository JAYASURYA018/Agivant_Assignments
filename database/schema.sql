-- Schema Script for MySQL and H2 compatibility

-- Drop tables if they exist (for clean re-runs)
DROP TABLE IF EXISTS leave_request;
DROP TABLE IF EXISTS holiday;
DROP TABLE IF EXISTS employee;

-- Create Employee Table
CREATE TABLE employee (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id VARCHAR(50) UNIQUE NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    department VARCHAR(50),
    role VARCHAR(20) DEFAULT 'EMPLOYEE' NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create Holiday Table
CREATE TABLE holiday (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    holiday_date DATE NOT NULL UNIQUE,
    description VARCHAR(255),
    holiday_type VARCHAR(50) DEFAULT 'PUBLIC'
);

-- Create Leave Request Table
CREATE TABLE leave_request (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    leave_type VARCHAR(10) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    number_of_days INT NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    reason VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employee(id) ON DELETE CASCADE,
    CONSTRAINT chk_leave_type CHECK (leave_type IN ('CL', 'SL', 'EL')),
    CONSTRAINT chk_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED')),
    CONSTRAINT chk_dates CHECK (start_date <= end_date)
);
