package com.perpustakaan.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.perpustakaan.model.BorrowTransaction;
import com.perpustakaan.model.User;
import com.perpustakaan.repository.BorrowTransactionRepository;
import java.util.List;
import java.util.Optional;

@Service
public class BorrowTransactionService {
    
    @Autowired
    private BorrowTransactionRepository borrowTransactionRepository;
    
    public List<BorrowTransaction> getAllTransactions() {
        return borrowTransactionRepository.findAll();
    }

    public List<BorrowTransaction> getTransactionsByUser(User user) {
        return borrowTransactionRepository.findByUser(user);
    }
    
    public Optional<BorrowTransaction> getTransactionById(Long id) {
        return borrowTransactionRepository.findById(id);
    }
    
    public BorrowTransaction saveBorrowTransaction(BorrowTransaction transaction) {
        transaction.borrow(); // Set status dan tanggal pinjam
        return borrowTransactionRepository.save(transaction);
    }
    
    public BorrowTransaction returnBook(Long id) {
        Optional<BorrowTransaction> transaction = borrowTransactionRepository.findById(id);
        if (transaction.isPresent()) {
            BorrowTransaction existingTransaction = transaction.get();
            existingTransaction.returnBook(); // Set status dan tanggal kembali
            return borrowTransactionRepository.save(existingTransaction);
        }
        return null;
    }
    
    public void deleteTransaction(Long id) {
        borrowTransactionRepository.deleteById(id);
    }

    public long getActiveBorrowedCount(User user) {
        return borrowTransactionRepository.countByUserAndStatus(user, "BORROWED");
    }

    public long getTotalTransactions(User user) {
        return borrowTransactionRepository.countByUser(user);
    }

    public List<BorrowTransaction> getActiveBorrowings(User user) {
        return borrowTransactionRepository.findByUserAndStatus(user, "BORROWED");
    }

    public List<BorrowTransaction> getActiveTransactionsByUser(User user) {
        return borrowTransactionRepository.findByUserAndStatus(user, "BORROWED");
    }
    
    public List<BorrowTransaction> getAllTransactionsByUser(User user) {
        return borrowTransactionRepository.findByUser(user);
    }
} 