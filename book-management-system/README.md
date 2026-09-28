# Online Book Management System

A beginner-friendly Book Management System built with Java 17, Spring Boot, Spring Data JPA, MySQL, HTML, CSS and JavaScript.

## Features

- Add, view, update and delete books
- Search books by title
- Manage authors, categories and borrowers
- Borrow and return books
- Prevent borrowing when available copies are zero
- SQL reports for availability, author, category, current loans and most borrowed book
- Basic JUnit 5 tests
- Simple frontend served by Spring Boot

## Project structure

```text
src/main/java
  model/          Entity classes
  repository/     Spring Data JPA repositories
  controller/     REST APIs
src/main/resources/static
  index.html
  style.css
  app.js
database/
  schema.sql
```

## Requirements

- Java 17+
- Maven 3.8+
- MySQL 8+

## Database setup

1. Start MySQL.
2. Open `database/schema.sql` in MySQL Workbench and run it.
3. The default application settings expect:

```text
database: book_management
username: root
password: root
```

If your MySQL password is different, edit:

`src/main/resources/application.properties`

## Run

From the project folder:

```bash
mvn spring-boot:run
```

Then open:

http://localhost:8080

## API examples

### Get all books

```http
GET /api/books
```

### Add a book

```http
POST /api/books
Content-Type: application/json

{
  "title": "Clean Code",
  "authorId": 1,
  "categoryId": 1,
  "totalCopies": 5,
  "availableCopies": 5
}
```

### Borrow

```http
POST /api/books/1/borrow
Content-Type: application/json

{
  "borrowerId": 1
}
```

### Return

```http
POST /api/books/1/return
```

### Search

```http
GET /api/books/search?keyword=clean
```

## Simple explanation for presentation

1. `Book`, `Author`, `Category`, `Borrower` and `Loan` are JPA entities, so Java classes are mapped to MySQL tables.
2. Repositories extend `JpaRepository`, which gives CRUD operations without writing SQL for basic operations.
3. `BookController` exposes REST APIs.
4. When a book is borrowed, `availableCopies` decreases by one and `borrowCount` increases by one.
5. When a book is returned, `availableCopies` increases by one and the loan gets a return date.
6. If `availableCopies` is zero, the borrow API returns a clear error.
7. The frontend uses JavaScript `fetch()` to call the backend APIs and dynamically render books.

## Notes

This project intentionally keeps the architecture simple for a beginner presentation. It avoids unnecessary service/DTO/security layers while still demonstrating Spring Boot, JPA, REST APIs, SQL and JavaScript.
