package com.perpustakaan.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.perpustakaan.model.BorrowTransaction;
import com.perpustakaan.model.User;
import java.util.List;

public interface BorrowTransactionRepository extends JpaRepository<BorrowTransaction, Long> {
    List<BorrowTransaction> findByUser(User user);
    List<BorrowTransaction> findByUserAndStatus(User user, String status);
    long countByUserAndStatus(User user, String status);
    long countByUser(User user);
} 