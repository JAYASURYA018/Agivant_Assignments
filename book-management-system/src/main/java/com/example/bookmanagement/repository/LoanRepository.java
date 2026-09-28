package com.example.bookmanagement.repository;

import com.example.bookmanagement.model.Loan;
import org.springframework.data.jpa.repository.*;
import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByReturnDateIsNull();
    boolean existsByBookIdAndReturnDateIsNull(Long bookId);
    Loan findFirstByBookIdAndReturnDateIsNull(Long bookId);
}
