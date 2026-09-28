package com.example.bookmanagement.repository;

import com.example.bookmanagement.model.Book;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {
    List<Book> findByTitleContainingIgnoreCase(String title);
    List<Book> findByAuthorNameContainingIgnoreCase(String authorName);

    @Query("select b from Book b where b.availableCopies > 0")
    List<Book> findAvailableBooks();

    @Query("select b.category.name, count(b) from Book b group by b.category.name")
    List<Object[]> countBooksByCategory();

    @Query("select b from Book b order by b.borrowCount desc")
    List<Book> findMostBorrowed();
}
