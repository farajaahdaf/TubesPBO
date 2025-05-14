package com.perpustakaan.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Sort;
import com.perpustakaan.model.BorrowTransaction;
import com.perpustakaan.model.User;
import java.time.LocalDateTime;
import java.util.List;

public interface BorrowTransactionRepository extends JpaRepository<BorrowTransaction, Long> {
    List<BorrowTransaction> findByUser(User user);
    List<BorrowTransaction> findByUserAndStatus(User user, String status);
    List<BorrowTransaction> findByStatus(String status, Sort sort);
    long countByUserAndStatus(User user, String status);
    long countByUser(User user);
    long countByStatusAndBorrowDateBefore(String status, LocalDateTime date);
} 