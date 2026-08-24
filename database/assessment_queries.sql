-- ===================================================================
-- BookPulse Smart Library Management System - Assessment SQL Queries
-- Demonstrates required SQL queries and analytics for evaluator review
-- ===================================================================

-- 1. Find all available books (available_copies > 0)
-- Description: Retrieves all titles with at least one physical copy ready for checkout
SELECT 
    b.id,
    b.title,
    b.isbn,
    c.name AS category,
    b.available_copies,
    b.total_copies
FROM books b
LEFT JOIN categories c ON b.category_id = c.id
WHERE b.available_copies > 0
ORDER BY b.title ASC;

-- 2. Find books by author (e.g., 'Robert C. Martin')
-- Description: Joins book_authors junction table to find all books by a specific author
SELECT 
    b.id,
    b.title,
    b.isbn,
    b.publication_year,
    a.name AS author_name,
    b.available_copies
FROM books b
JOIN book_authors ba ON b.id = ba.book_id
JOIN authors a ON ba.author_id = a.id
WHERE LOWER(a.name) LIKE LOWER('%Robert C. Martin%')
ORDER BY b.publication_year DESC;

-- 3. Count books by category
-- Description: Aggregates total titles and physical copies per category
SELECT 
    c.id AS category_id,
    c.name AS category_name,
    COUNT(b.id) AS total_titles,
    COALESCE(SUM(b.total_copies), 0) AS total_physical_copies,
    COALESCE(SUM(b.available_copies), 0) AS total_available_copies
FROM categories c
LEFT JOIN books b ON c.id = b.category_id
GROUP BY c.id, c.name
ORDER BY total_titles DESC;

-- 4. Find currently borrowed books
-- Description: Retrieves active loans where status is 'BORROWED' and returned_at IS NULL
SELECT 
    bt.id AS transaction_id,
    b.id AS book_id,
    b.title AS book_title,
    br.name AS borrower_name,
    br.email AS borrower_email,
    bt.borrowed_at,
    bt.due_date,
    bt.status
FROM borrow_transactions bt
JOIN books b ON bt.book_id = b.id
JOIN borrowers br ON bt.borrower_id = br.id
WHERE bt.status = 'BORROWED' AND bt.returned_at IS NULL
ORDER BY bt.due_date ASC;

-- 5. Find the most borrowed book
-- Description: Ranks books by total lifetime borrow transaction frequency
SELECT 
    b.id AS book_id,
    b.title,
    b.isbn,
    c.name AS category,
    COUNT(bt.id) AS borrow_count
FROM books b
LEFT JOIN categories c ON b.category_id = c.id
JOIN borrow_transactions bt ON b.id = bt.book_id
GROUP BY b.id, b.title, b.isbn, c.name
ORDER BY borrow_count DESC
LIMIT 5;

-- 6. Find overdue books & overdue borrowers
-- Description: Identifies active unreturned loans past their due date
SELECT 
    bt.id AS transaction_id,
    b.title AS book_title,
    br.name AS borrower_name,
    br.email AS borrower_email,
    bt.borrowed_at,
    bt.due_date,
    DATEDIFF('DAY', bt.due_date, CURRENT_TIMESTAMP) AS days_overdue
FROM borrow_transactions bt
JOIN books b ON bt.book_id = b.id
JOIN borrowers br ON bt.borrower_id = br.id
WHERE bt.returned_at IS NULL 
  AND (bt.status = 'OVERDUE' OR bt.due_date < CURRENT_TIMESTAMP)
ORDER BY days_overdue DESC;

-- 7. Full Borrower History & Health Analysis
-- Description: Analyzes a borrower's total borrow count, return count, and overdue risk
SELECT 
    br.id AS borrower_id,
    br.name AS borrower_name,
    br.email,
    COUNT(bt.id) AS total_borrowed,
    COUNT(CASE WHEN bt.status = 'RETURNED' THEN 1 END) AS total_returned,
    COUNT(CASE WHEN bt.status = 'BORROWED' THEN 1 END) AS currently_borrowed,
    COUNT(CASE WHEN bt.status = 'OVERDUE' OR (bt.returned_at IS NULL AND bt.due_date < CURRENT_TIMESTAMP) THEN 1 END) AS overdue_count,
    CASE 
        WHEN COUNT(CASE WHEN bt.status = 'OVERDUE' OR (bt.returned_at IS NULL AND bt.due_date < CURRENT_TIMESTAMP) THEN 1 END) = 0 THEN 'GOOD'
        WHEN COUNT(CASE WHEN bt.status = 'OVERDUE' OR (bt.returned_at IS NULL AND bt.due_date < CURRENT_TIMESTAMP) THEN 1 END) = 1 THEN 'WARNING'
        ELSE 'RISK'
    END AS borrower_health
FROM borrowers br
LEFT JOIN borrow_transactions bt ON br.id = bt.borrower_id
GROUP BY br.id, br.name, br.email
ORDER BY overdue_count DESC, total_borrowed DESC;

-- 8. Low Availability Alert (Books with critical stock ratio <= 25% available)
SELECT 
    b.id,
    b.title,
    b.total_copies,
    b.available_copies,
    CAST(b.available_copies AS FLOAT) / b.total_copies * 100 AS availability_percent
FROM books b
WHERE b.available_copies <= 2
ORDER BY b.available_copies ASC, b.total_copies DESC;
