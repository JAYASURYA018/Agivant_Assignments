INSERT INTO authors (name) SELECT 'Robert C. Martin' WHERE NOT EXISTS (SELECT 1 FROM authors WHERE name='Robert C. Martin');
INSERT INTO authors (name) SELECT 'J.K. Rowling' WHERE NOT EXISTS (SELECT 1 FROM authors WHERE name='J.K. Rowling');
INSERT INTO authors (name) SELECT 'George Orwell' WHERE NOT EXISTS (SELECT 1 FROM authors WHERE name='George Orwell');

INSERT INTO categories (name) SELECT 'Programming' WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name='Programming');
INSERT INTO categories (name) SELECT 'Fantasy' WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name='Fantasy');
INSERT INTO categories (name) SELECT 'Classic' WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name='Classic');

INSERT INTO borrowers (name, email) SELECT 'Karthik', 'karthik@example.com' WHERE NOT EXISTS (SELECT 1 FROM borrowers WHERE email='karthik@example.com');
INSERT INTO borrowers (name, email) SELECT 'Rahul', 'rahul@example.com' WHERE NOT EXISTS (SELECT 1 FROM borrowers WHERE email='rahul@example.com');
INSERT INTO borrowers (name, email) SELECT 'Priya', 'priya@example.com' WHERE NOT EXISTS (SELECT 1 FROM borrowers WHERE email='priya@example.com');
