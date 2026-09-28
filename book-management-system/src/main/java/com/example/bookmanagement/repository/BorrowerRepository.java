package com.example.bookmanagement.repository;

import com.example.bookmanagement.model.Borrower;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BorrowerRepository extends JpaRepository<Borrower, Long> {}
