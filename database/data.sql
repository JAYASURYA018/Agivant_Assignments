-- ===================================================================
-- BookPulse Smart Library Management System - Seed Dataset
-- Rich dataset for books, authors, categories, borrowers, and transactions
-- ===================================================================

-- 1. Insert Categories
INSERT INTO categories (id, name, description) VALUES
(1, 'Technology & Programming', 'Software architecture, coding, algorithms, and engineering practices'),
(2, 'Fiction & Literature', 'Classic, contemporary, and celebrated literary works'),
(3, 'Science & Nature', 'Physics, cosmology, biology, and natural science discoveries'),
(4, 'History & Biography', 'World history, civilizations, and prominent memoirs'),
(5, 'Self-Help & Psychology', 'Productivity, cognitive habits, mindset, and human behavior'),
(6, 'Fantasy & Sci-Fi', 'Epic fantasy universes, space exploration, and speculative fiction');

-- 2. Insert Authors
INSERT INTO authors (id, name, bio) VALUES
(1, 'Robert C. Martin', 'Uncle Bob Martin is a software engineer, co-author of Agile Manifesto, and author of Clean Code series.'),
(2, 'Joshua Bloch', 'Former chief Java architect at Google and author of Effective Java.'),
(3, 'Martin Fowler', 'Chief Scientist at ThoughtWorks, author of Refactoring and Patterns of Enterprise Application Architecture.'),
(4, 'Erich Gamma', 'Software engineer, member of the Gang of Four (GoF), and lead on Eclipse & VS Code.'),
(5, 'George Orwell', 'English novelist, essayist, and critic famous for Animal Farm and 1984.'),
(6, 'Paulo Coelho', 'Brazilian lyricist and novelist, best known for his internationally acclaimed novel The Alchemist.'),
(7, 'Yuval Noah Harari', 'Historian, philosopher, and bestselling author of Sapiens: A Brief History of Humankind.'),
(8, 'James Clear', 'Author and speaker focused on habits, decision-making, and continuous improvement.'),
(9, 'Stephen Hawking', 'Theoretical physicist, cosmologist, and author of A Brief History of Time.'),
(10, 'Frank Herbert', 'American science fiction author best known for the epic 1965 novel Dune.');

-- 3. Insert Books
INSERT INTO books (id, title, isbn, description, publication_year, total_copies, available_copies, category_id, cover_image_url, created_at, updated_at) VALUES
(1, 'Clean Code: A Handbook of Agile Software Craftsmanship', '9780132350884', 'Even bad code can function. But if code isn''t clean, it can bring a development organization to its knees.', 2008, 5, 2, 1, 'https://images-na.ssl-images-amazon.com/images/I/41xShlnTZTL._SX376_BO1,204,203,200_.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'Effective Java', '9780134685991', 'The definitive guide to Java platform best practices by Java guru Joshua Bloch.', 2018, 4, 3, 1, 'https://images-na.ssl-images-amazon.com/images/I/41PLj0bwnxL._SX396_BO1,204,203,200_.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'Design Patterns: Elements of Reusable Object-Oriented Software', '9780201633610', 'Capturing a wealth of experience about the design of object-oriented software, four top-notch designers present a catalog of simple and succinct solutions.', 1994, 3, 1, 1, 'https://images-na.ssl-images-amazon.com/images/I/51szD9HC9pL._SX395_BO1,204,203,200_.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'Refactoring: Improving the Design of Existing Code', '9780134757599', 'Martin Fowler provides a fully updated guide to modern refactoring techniques in software design.', 2018, 4, 4, 1, 'https://images-na.ssl-images-amazon.com/images/I/41O5qNWm4vL._SX396_BO1,204,203,200_.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 'The Alchemist', '9780062315007', 'A magical fable about following your dream, listening to your heart, and reading the omens strewn along life''s path.', 1988, 6, 2, 2, 'https://images-na.ssl-images-amazon.com/images/I/51Z0nLAfLmL._SX320_BO1,204,203,200_.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, '1984', '9780451524935', 'A dystopian social science fiction novel and cautionary tale about totalitarianism and surveillance.', 1949, 5, 4, 2, 'https://images-na.ssl-images-amazon.com/images/I/41gVhoPaVIL._SX303_BO1,204,203,200_.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(7, 'Sapiens: A Brief History of Humankind', '9780062316097', 'A groundbreaking narrative of humanity''s creation and evolution that explores how biology and history have defined us.', 2014, 5, 3, 4, 'https://images-na.ssl-images-amazon.com/images/I/41+eK8zBwBL._SX329_BO1,204,203,200_.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(8, 'Atomic Habits', '9780735211292', 'An easy & proven way to build good habits and break bad ones through tiny, compound behavioral shifts.', 2018, 6, 2, 5, 'https://images-na.ssl-images-amazon.com/images/I/513Y5o-DYtL._SX330_BO1,204,203,200_.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(9, 'A Brief History of Time', '9780553380163', 'A landmark volume in science writing by one of the great minds of modern physics about the origins of the universe.', 1988, 3, 2, 3, 'https://images-na.ssl-images-amazon.com/images/I/51+GySc8ExL._SX331_BO1,204,203,200_.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(10, 'Dune', '9780441172719', 'Set on the desert planet Arrakis, Dune is the story of Paul Atreides and humanity''s grandest science fiction saga.', 1965, 4, 1, 6, 'https://images-na.ssl-images-amazon.com/images/I/41-q0fN5QcL._SX304_BO1,204,203,200_.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(11, 'Clean Architecture: A Craftsman''s Guide to Software Structure', '9780134494166', 'Practical software architecture rules and principles for programmers of all stripes by Uncle Bob Martin.', 2017, 3, 3, 1, 'https://images-na.ssl-images-amazon.com/images/I/41-sN-mzwKL._SX381_BO1,204,203,200_.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(12, 'Thinking, Fast and Slow', '9780374533557', 'Daniel Kahneman explains the two systems that drive the way we think: fast intuitive thinking, and slow deliberate thinking.', 2011, 4, 2, 5, 'https://images-na.ssl-images-amazon.com/images/I/41wI53OEpCL._SX322_BO1,204,203,200_.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 4. Insert Book-Author Relationships
INSERT INTO book_authors (book_id, author_id) VALUES
(1, 1),  -- Clean Code -> Robert C. Martin
(2, 2),  -- Effective Java -> Joshua Bloch
(3, 4),  -- Design Patterns -> Erich Gamma
(4, 3),  -- Refactoring -> Martin Fowler
(5, 6),  -- The Alchemist -> Paulo Coelho
(6, 5),  -- 1984 -> George Orwell
(7, 7),  -- Sapiens -> Yuval Noah Harari
(8, 8),  -- Atomic Habits -> James Clear
(9, 9),  -- A Brief History of Time -> Stephen Hawking
(10, 10), -- Dune -> Frank Herbert
(11, 1), -- Clean Architecture -> Robert C. Martin
(12, 7); -- Thinking Fast & Slow

-- 5. Insert Borrowers
INSERT INTO borrowers (id, name, email, phone, created_at) VALUES
(1, 'Likitha Nambari', 'likitha@example.com', '+91 9876543210', CURRENT_TIMESTAMP),
(2, 'Rahul Sharma', 'rahul.sharma@example.com', '+91 9876543211', CURRENT_TIMESTAMP),
(3, 'Priya Patel', 'priya.patel@example.com', '+91 9876543212', CURRENT_TIMESTAMP),
(4, 'John Doe', 'john.doe@example.com', '+1 555-0199', CURRENT_TIMESTAMP),
(5, 'Aisha Khan', 'aisha.khan@example.com', '+91 9876543213', CURRENT_TIMESTAMP),
(6, 'Alex Rivera', 'alex.rivera@example.com', '+1 555-0144', CURRENT_TIMESTAMP),
(7, 'David Chen', 'david.chen@example.com', '+1 555-0188', CURRENT_TIMESTAMP),
(8, 'Sarah Jenkins', 'sarah.jenkins@example.com', '+1 555-0177', CURRENT_TIMESTAMP);

-- 6. Insert Borrow Transactions (Historical & Active)
INSERT INTO borrow_transactions (id, book_id, borrower_id, borrowed_at, due_date, returned_at, status) VALUES
-- Historical Returned Loans (Clean Code)
(1, 1, 1, TIMESTAMP '2026-06-01 10:00:00', TIMESTAMP '2026-06-15 10:00:00', TIMESTAMP '2026-06-12 14:30:00', 'RETURNED'),
(2, 1, 2, TIMESTAMP '2026-06-16 11:00:00', TIMESTAMP '2026-06-30 11:00:00', TIMESTAMP '2026-06-25 09:15:00', 'RETURNED'),
(3, 1, 3, TIMESTAMP '2026-07-01 12:00:00', TIMESTAMP '2026-07-15 12:00:00', TIMESTAMP '2026-07-14 16:45:00', 'RETURNED'),
-- Historical Returned Loans (The Alchemist - Most Borrowed Book)
(4, 5, 2, TIMESTAMP '2026-05-10 09:00:00', TIMESTAMP '2026-05-24 09:00:00', TIMESTAMP '2026-05-20 11:00:00', 'RETURNED'),
(5, 5, 4, TIMESTAMP '2026-06-02 10:30:00', TIMESTAMP '2026-06-16 10:30:00', TIMESTAMP '2026-06-15 15:00:00', 'RETURNED'),
(6, 5, 5, TIMESTAMP '2026-06-20 14:00:00', TIMESTAMP '2026-07-04 14:00:00', TIMESTAMP '2026-07-01 17:20:00', 'RETURNED'),
(7, 5, 1, TIMESTAMP '2026-07-05 10:00:00', TIMESTAMP '2026-07-19 10:00:00', TIMESTAMP '2026-07-18 12:00:00', 'RETURNED'),
-- Historical Returned Loans (Atomic Habits)
(8, 8, 2, TIMESTAMP '2026-06-10 11:00:00', TIMESTAMP '2026-06-24 11:00:00', TIMESTAMP '2026-06-22 13:00:00', 'RETURNED'),
(9, 8, 3, TIMESTAMP '2026-07-01 09:00:00', TIMESTAMP '2026-07-15 09:00:00', TIMESTAMP '2026-07-10 10:00:00', 'RETURNED'),
(10, 8, 6, TIMESTAMP '2026-07-16 15:00:00', TIMESTAMP '2026-07-30 15:00:00', TIMESTAMP '2026-07-28 16:30:00', 'RETURNED'),
-- Active Currently Borrowed Loans
(11, 1, 4, TIMESTAMP '2026-08-15 10:00:00', TIMESTAMP '2026-08-29 10:00:00', NULL, 'BORROWED'),
(12, 1, 5, TIMESTAMP '2026-08-18 14:00:00', TIMESTAMP '2026-09-01 14:00:00', NULL, 'BORROWED'),
(13, 1, 6, TIMESTAMP '2026-08-20 11:30:00', TIMESTAMP '2026-09-03 11:30:00', NULL, 'BORROWED'),
(14, 2, 1, TIMESTAMP '2026-08-16 09:30:00', TIMESTAMP '2026-08-30 09:30:00', NULL, 'BORROWED'),
(15, 3, 2, TIMESTAMP '2026-08-14 16:00:00', TIMESTAMP '2026-08-28 16:00:00', NULL, 'BORROWED'),
(16, 3, 3, TIMESTAMP '2026-08-17 12:00:00', TIMESTAMP '2026-08-31 12:00:00', NULL, 'BORROWED'),
(17, 5, 7, TIMESTAMP '2026-08-19 15:00:00', TIMESTAMP '2026-09-02 15:00:00', NULL, 'BORROWED'),
(18, 5, 8, TIMESTAMP '2026-08-21 10:00:00', TIMESTAMP '2026-09-04 10:00:00', NULL, 'BORROWED'),
(19, 5, 2, TIMESTAMP '2026-08-21 16:00:00', TIMESTAMP '2026-09-04 16:00:00', NULL, 'BORROWED'),
(20, 5, 3, TIMESTAMP '2026-08-22 11:00:00', TIMESTAMP '2026-09-05 11:00:00', NULL, 'BORROWED'),
(21, 8, 4, TIMESTAMP '2026-08-15 14:00:00', TIMESTAMP '2026-08-29 14:00:00', NULL, 'BORROWED'),
(22, 8, 5, TIMESTAMP '2026-08-19 10:30:00', TIMESTAMP '2026-09-02 10:30:00', NULL, 'BORROWED'),
(23, 8, 6, TIMESTAMP '2026-08-20 13:00:00', TIMESTAMP '2026-09-03 13:00:00', NULL, 'BORROWED'),
(24, 8, 7, TIMESTAMP '2026-08-21 12:00:00', TIMESTAMP '2026-09-04 12:00:00', NULL, 'BORROWED'),
(25, 10, 8, TIMESTAMP '2026-08-10 09:00:00', TIMESTAMP '2026-08-24 09:00:00', NULL, 'BORROWED'),
(26, 10, 4, TIMESTAMP '2026-08-12 15:00:00', TIMESTAMP '2026-08-26 15:00:00', NULL, 'BORROWED'),
(27, 10, 5, TIMESTAMP '2026-08-14 11:00:00', TIMESTAMP '2026-08-28 11:00:00', NULL, 'BORROWED'),
-- Overdue Loan to showcase OVERDUE risk alerts (due before current date Aug 22, 2026)
(28, 6, 7, TIMESTAMP '2026-07-20 10:00:00', TIMESTAMP '2026-08-03 10:00:00', NULL, 'OVERDUE'),
(29, 7, 7, TIMESTAMP '2026-07-25 14:00:00', TIMESTAMP '2026-08-08 14:00:00', NULL, 'OVERDUE'),
(30, 9, 8, TIMESTAMP '2026-08-01 09:00:00', TIMESTAMP '2026-08-15 09:00:00', NULL, 'OVERDUE');
