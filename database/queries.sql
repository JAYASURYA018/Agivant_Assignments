-- SQL Task Queries for Leave Management System

-- Query 1: Find employees with more than 5 leave days.
SELECT e.id, e.first_name, e.last_name, SUM(lr.number_of_days) as total_approved_leave_days
FROM employee e
JOIN leave_request lr ON e.id = lr.employee_id
WHERE lr.status = 'APPROVED'
GROUP BY e.id, e.first_name, e.last_name
HAVING SUM(lr.number_of_days) > 5;


-- Query 2: Calculate total leave taken by each employee.

SELECT e.id, e.first_name, e.last_name, COALESCE(SUM(lr.number_of_days), 0) as total_leaves_taken
FROM employee e
LEFT JOIN leave_request lr ON e.id = lr.employee_id AND lr.status = 'APPROVED'
GROUP BY e.id, e.first_name, e.last_name;


-- Query 3: Find pending leave requests.

SELECT lr.id, e.first_name, e.last_name, lr.leave_type, lr.start_date, lr.end_date, lr.number_of_days, lr.reason
FROM leave_request lr
JOIN employee e ON lr.employee_id = e.id
WHERE lr.status = 'PENDING';


-- Query 4: Find employees with no leave requests.

SELECT e.id, e.first_name, e.last_name, e.email, e.department
FROM employee e
WHERE NOT EXISTS (
    SELECT 1 
    FROM leave_request lr 
    WHERE lr.employee_id = e.id
);

-- Query 5: Find the most frequently used leave type.

SELECT leave_type, COUNT(*) as usage_count
FROM leave_request
GROUP BY leave_type
ORDER BY usage_count DESC
LIMIT 1;
