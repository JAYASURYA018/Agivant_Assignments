package com.bookpulse.repository;

import com.bookpulse.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByIsbn(String isbn);
    boolean existsByIsbn(String isbn);
    boolean existsByIsbnAndIdNot(String isbn, Long id);

    List<Book> findByAvailableCopiesGreaterThan(Integer copies);

    List<Book> findByCategoryId(Long categoryId);

    List<Book> findByAuthorsId(Long authorId);

    @Query("SELECT DISTINCT b FROM Book b " +
           "LEFT JOIN b.category c " +
           "LEFT JOIN b.authors a " +
           "WHERE (:query IS NULL OR :query = '' OR " +
           "       LOWER(b.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "       LOWER(b.isbn) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "       LOWER(b.description) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "       LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "       LOWER(a.name) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "AND (:categoryId IS NULL OR c.id = :categoryId) " +
           "AND (:availableOnly IS NULL OR :availableOnly = false OR b.availableCopies > 0)")
    List<Book> searchBooks(
            @Param("query") String query,
            @Param("categoryId") Long categoryId,
            @Param("availableOnly") Boolean availableOnly
    );

    @Query("SELECT b FROM Book b WHERE b.category.id = :categoryId AND b.id != :bookId")
    List<Book> findSimilarCategoryBooks(@Param("categoryId") Long categoryId, @Param("bookId") Long bookId);

    @Query("SELECT DISTINCT b FROM Book b JOIN b.authors a WHERE a.id IN :authorIds AND b.id != :bookId")
    List<Book> findSimilarAuthorBooks(@Param("authorIds") List<Long> authorIds, @Param("bookId") Long bookId);

    @Query("SELECT b FROM Book b WHERE b.availableCopies <= 2 ORDER BY b.availableCopies ASC")
    List<Book> findLowAvailabilityBooks();

    @Query("SELECT COUNT(b) FROM Book b")
    long countTotalTitles();

    @Query("SELECT COALESCE(SUM(b.totalCopies), 0) FROM Book b")
    long countTotalCopies();

    @Query("SELECT COALESCE(SUM(b.availableCopies), 0) FROM Book b")
    long countAvailableCopies();
}
