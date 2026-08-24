package com.bookpulse.repository;

import com.bookpulse.entity.BorrowTransaction;
import com.bookpulse.entity.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BorrowTransactionRepository extends JpaRepository<BorrowTransaction, Long> {

    List<BorrowTransaction> findByBookIdOrderByBorrowedAtDesc(Long bookId);

    List<BorrowTransaction> findByBorrowerIdOrderByBorrowedAtDesc(Long borrowerId);

    List<BorrowTransaction> findByStatusOrderByDueDateAsc(TransactionStatus status);

    @Query("SELECT bt FROM BorrowTransaction bt WHERE bt.book.id = :bookId AND bt.borrower.id = :borrowerId AND bt.status = 'BORROWED'")
    Optional<BorrowTransaction> findActiveTransaction(@Param("bookId") Long bookId, @Param("borrowerId") Long borrowerId);

    @Query("SELECT bt FROM BorrowTransaction bt WHERE bt.book.id = :bookId AND bt.status = 'BORROWED'")
    List<BorrowTransaction> findActiveTransactionsForBook(@Param("bookId") Long bookId);

    @Query("SELECT bt FROM BorrowTransaction bt WHERE bt.status = 'BORROWED' AND bt.dueDate < :now")
    List<BorrowTransaction> findOverdueTransactions(@Param("now") LocalDateTime now);

    @Query("SELECT bt.book.id, bt.book.title, bt.book.isbn, bt.book.coverImageUrl, COUNT(bt.id) " +
           "FROM BorrowTransaction bt " +
           "GROUP BY bt.book.id, bt.book.title, bt.book.isbn, bt.book.coverImageUrl " +
           "ORDER BY COUNT(bt.id) DESC")
    List<Object[]> findMostBorrowedBooksRaw();

    @Query("SELECT c.id, c.name, COUNT(bt.id) " +
           "FROM BorrowTransaction bt " +
           "JOIN bt.book b " +
           "JOIN b.category c " +
           "GROUP BY c.id, c.name " +
           "ORDER BY COUNT(bt.id) DESC")
    List<Object[]> findCategoryBorrowStatsRaw();

    @Query("SELECT bt.borrower.id, bt.borrower.name, bt.borrower.email, COUNT(bt.id) " +
           "FROM BorrowTransaction bt " +
           "GROUP BY bt.borrower.id, bt.borrower.name, bt.borrower.email " +
           "ORDER BY COUNT(bt.id) DESC")
    List<Object[]> findTopBorrowersRaw();

    @Query("SELECT bt2.book.id, COUNT(bt2.id) " +
           "FROM BorrowTransaction bt1 " +
           "JOIN BorrowTransaction bt2 ON bt1.borrower.id = bt2.borrower.id " +
           "WHERE bt1.book.id = :bookId AND bt2.book.id != :bookId " +
           "GROUP BY bt2.book.id " +
           "ORDER BY COUNT(bt2.id) DESC")
    List<Object[]> findFrequentlyCoBorrowedBookIds(@Param("bookId") Long bookId);

    long countByStatus(TransactionStatus status);
}
