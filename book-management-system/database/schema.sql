CREATE DATABASE IF NOT EXISTS book_management;
USE book_management;

CREATE TABLE IF NOT EXISTS authors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS borrowers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS books (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    total_copies INT NOT NULL,
    available_copies INT NOT NULL,
    borrow_count INT NOT NULL DEFAULT 0,
    FOREIGN KEY (author_id) REFERENCES authors(id),
    FOREIGN KEY (category_id) REFERENCES categories(id)
);

CREATE TABLE IF NOT EXISTS loans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    book_id BIGINT NOT NULL,
    borrower_id BIGINT NOT NULL,
    borrow_date DATE NOT NULL,
    return_date DATE NULL,
    FOREIGN KEY (book_id) REFERENCES books(id),
    FOREIGN KEY (borrower_id) REFERENCES borrowers(id)
);

-- Required SQL tasks

-- 1. Find all available books
SELECT * FROM books WHERE available_copies > 0;

-- 2. Find books by author
SELECT b.* FROM books b
JOIN authors a ON b.author_id = a.id
WHERE a.name = 'Robert C. Martin';

-- 3. Count books by category
SELECT c.name, COUNT(b.id) AS book_count
FROM categories c
LEFT JOIN books b ON b.category_id = c.id
GROUP BY c.id, c.name;

-- 4. Find currently borrowed books
SELECT b.title, br.name AS borrower, l.borrow_date
FROM loans l
JOIN books b ON l.book_id = b.id
JOIN borrowers br ON l.borrower_id = br.id
WHERE l.return_date IS NULL;

-- 5. Find the most borrowed book
SELECT * FROM books ORDER BY borrow_count DESC LIMIT 1;
