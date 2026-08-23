-- Seed Data for Spring Boot Auto-Initialization

-- Insert Sample Employees
INSERT INTO employee (employee_id, first_name, last_name, email, password, department, role) VALUES
('Z1012', 'Liam John', 'Liam John', 'liamjohn@zylker.com', '$2a$10$8.UnVuG9HHgffUDAlk8qCOuy5ymDFGCo0egN9dB7m982gFMsn34Py', NULL, 'EMPLOYEE'),
('Z1011', 'Joe', 'Smith', 'joesmith@zylker.com', '$2a$10$8.UnVuG9HHgffUDAlk8qCOuy5ymDFGCo0egN9dB7m982gFMsn34Py', NULL, 'EMPLOYEE'),
('Z1010', 'Rachael', 'Matthew', 'rachael.matthew@zylker.com', '$2a$10$8.UnVuG9HHgffUDAlk8qCOuy5ymDFGCo0egN9dB7m982gFMsn34Py', NULL, 'EMPLOYEE'),
('HRM20', 'Anupriya', 'Mohan', 'anupriya.mohan@zylker.com', '$2a$10$8.UnVuG9HHgffUDAlk8qCOuy5ymDFGCo0egN9dB7m982gFMsn34Py', NULL, 'MANAGER'),
('HRM19', 'Regina', 'Lee', 'regina.lee@zylker.com', '$2a$10$8.UnVuG9HHgffUDAlk8qCOuy5ymDFGCo0egN9dB7m982gFMsn34Py', NULL, 'MANAGER'),
('HRM18', 'Jen', 'Adams', 'jen.adams@zylker.com', '$2a$10$8.UnVuG9HHgffUDAlk8qCOuy5ymDFGCo0egN9dB7m982gFMsn34Py', NULL, 'EMPLOYEE'),
('HRM17', 'Amelia', 'Brandon', 'amelia.br@zylker.com', '$2a$10$8.UnVuG9HHgffUDAlk8qCOuy5ymDFGCo0egN9dB7m982gFMsn34Py', NULL, 'EMPLOYEE'),
('1244', 'Tina', 'Francis', 'tina.francis@zylker.com', '$2a$10$8.UnVuG9HHgffUDAlk8qCOuy5ymDFGCo0egN9dB7m982gFMsn34Py', NULL, 'EMPLOYEE'),
('1243', 'Tayloenne', 'Tayloenne', 'tayloenne@gmail.com', '$2a$10$8.UnVuG9HHgffUDAlk8qCOuy5ymDFGCo0egN9dB7m982gFMsn34Py', 'Media', 'EMPLOYEE');

-- Insert Holidays (Matching the dashboard screenshot for year 2026)
INSERT INTO holiday (name, holiday_date, description, holiday_type) VALUES
('New Year', '2026-01-01', 'New Year Day', 'PUBLIC'),
('Sankranti/Pongal', '2026-01-15', 'Harvest Festival', 'PUBLIC'),
('Republic Day', '2026-01-26', 'Republic Day of India', 'PUBLIC'),
('Holi', '2026-03-03', 'Festival of Colors', 'PUBLIC'),
('Ugadi / Gudi Padwa', '2026-03-19', 'Telugu/Marathi New Year', 'PUBLIC'),
('Labour Day', '2026-05-01', 'May Day / Labour Day', 'PUBLIC'),
('Bakrid / Eid-ul-Adha (Date Tentative)', '2026-05-28', 'Festival of Sacrifice', 'PUBLIC'),
('Independence Day', '2026-08-15', 'Independence Day of India', 'PUBLIC'),
('Ganesh Chaturthi', '2026-09-14', 'Festival of Lord Ganesha', 'PUBLIC'),
('Gandhi Jayanti', '2026-10-02', 'Birthday of Mahatma Gandhi', 'PUBLIC'),
('Dussehra / Vijaya Dashami', '2026-10-20', 'Victory of Good over Evil', 'PUBLIC'),
('Diwali', '2026-11-08', 'Festival of Lights - Day 1', 'PUBLIC'),
('Diwali', '2026-11-09', 'Festival of Lights - Day 2', 'PUBLIC'),
('Christmas', '2026-12-25', 'Christmas Day', 'PUBLIC');

-- Insert Sample Leave Requests
INSERT INTO leave_request (employee_id, leave_type, start_date, end_date, number_of_days, status, reason) VALUES
(1, 'CL', '2026-08-10', '2026-08-11', 2, 'APPROVED', 'Personal work'),
(1, 'SL', '2026-08-18', '2026-08-18', 1, 'APPROVED', 'Fever'),
(2, 'EL', '2026-09-01', '2026-09-05', 5, 'PENDING', 'Annual family trip'),
(3, 'CL', '2026-08-20', '2026-08-20', 1, 'REJECTED', 'Urgent work at home');
