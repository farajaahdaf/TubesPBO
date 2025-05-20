package com.perpustakaan.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.perpustakaan.model.BorrowTransaction;
import com.perpustakaan.model.User;
import com.perpustakaan.model.Book;
import com.perpustakaan.repository.BorrowTransactionRepository;
import com.perpustakaan.repository.BookRepository;
import com.perpustakaan.repository.UserRepository;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@Service
public class BorrowTransactionService {
    
    @Autowired
    private BorrowTransactionRepository borrowTransactionRepository;
    
    @Autowired
    private BookRepository bookRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AuthService authService;
    
    private static final int MAX_BORROW_MINUTES = 2; // Maksimal peminjaman 2 menit untuk testing
    private static final int PERIOD_MINUTES = 2; // Periode denda (2 menit)
    private static final double FINE_PER_PERIOD = 10000.0; // Denda per periode (Rp 10.000)
    
    public List<BorrowTransaction> getAllTransactions() {
        return borrowTransactionRepository.findAll();
    }

    public List<BorrowTransaction> getTransactionsByUser(User user) {
        return borrowTransactionRepository.findByUser(user);
    }
    
    public Optional<BorrowTransaction> getTransactionById(Long id) {
        return borrowTransactionRepository.findById(id);
    }
    
    @Transactional
    public BorrowTransaction saveBorrowTransaction(BorrowTransaction transaction) {
        Book book = transaction.getBook();
        if (book != null && book.getStock() > 0) {
            // Update book stock
            book.setStock(book.getStock() - 1);
            bookRepository.save(book);
            
            // Set transaction status and date
            transaction.setStatus("BORROWED");
            transaction.setBorrowDate(LocalDateTime.now());
            
            return borrowTransactionRepository.save(transaction);
        }
        throw new RuntimeException("Buku tidak tersedia untuk dipinjam");
    }
    
    @Transactional
    public BorrowTransaction returnBook(Long id) {
        Optional<BorrowTransaction> transactionOpt = borrowTransactionRepository.findById(id);
        if (transactionOpt.isPresent()) {
            BorrowTransaction transaction = transactionOpt.get();
            Book book = transaction.getBook();
            User user = transaction.getUser();
            
            // Only process if status is still BORROWED
            if ("BORROWED".equals(transaction.getStatus())) {
                // Update book stock
                book.setStock(book.getStock() + 1);
                bookRepository.save(book);
                
                // Calculate fine if late
                LocalDateTime returnDate = LocalDateTime.now();
                LocalDateTime borrowDate = transaction.getBorrowDate();
                LocalDateTime dueDate = borrowDate.plusMinutes(MAX_BORROW_MINUTES);
                
                if (returnDate.isAfter(dueDate)) {
                    long secondsLate = java.time.Duration.between(dueDate, returnDate).getSeconds();
                    long minutesLate = (long) Math.ceil(secondsLate / 60.0);
                    if (minutesLate > 0) {
                        double fine = minutesLate * FINE_PER_PERIOD;
                        authService.updateUserFine(user, fine);
                    }
                }
                
                // Update transaction status and return date
                transaction.setStatus("RETURNED");
                transaction.setReturnDate(returnDate);
                return borrowTransactionRepository.save(transaction);
            } else {
                // If already returned, just return the transaction (no double fine)
                return transaction;
            }
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

    // New methods for admin dashboard
    public List<BorrowTransaction> getAllActiveTransactions() {
        return borrowTransactionRepository.findByStatus("BORROWED", Sort.by(Sort.Direction.DESC, "borrowDate"));
    }

    public List<BorrowTransaction> getRecentTransactions(int limit) {
        return borrowTransactionRepository.findAll(
            PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "borrowDate"))
        ).getContent();
    }

    public long getOverdueTransactionsCount() {
        LocalDateTime now = LocalDateTime.now();
        return borrowTransactionRepository.countByStatusAndBorrowDateBefore("BORROWED", now.minusDays(14));
    }

    public double calculateTotalFine(User user) {
        List<BorrowTransaction> transactions = borrowTransactionRepository.findByUser(user);
        double totalFine = 0.0;
        LocalDateTime now = LocalDateTime.now();
        for (BorrowTransaction transaction : transactions) {
            LocalDateTime borrowDate = transaction.getBorrowDate();
            LocalDateTime dueDate = borrowDate.plusMinutes(MAX_BORROW_MINUTES);
            if ("RETURNED".equals(transaction.getStatus())) {
                LocalDateTime returnDate = transaction.getReturnDate();
                if (returnDate != null && returnDate.isAfter(dueDate)) {
                    long secondsLate = java.time.Duration.between(dueDate, returnDate).getSeconds();
                    long periodsLate = (long) Math.ceil(secondsLate / (PERIOD_MINUTES * 60.0));
                    if (periodsLate > 0) {
                        totalFine += periodsLate * FINE_PER_PERIOD;
                    }
                }
            } else if ("BORROWED".equals(transaction.getStatus()) && now.isAfter(dueDate)) {
                long secondsLate = java.time.Duration.between(dueDate, now).getSeconds();
                long periodsLate = (long) Math.ceil(secondsLate / (PERIOD_MINUTES * 60.0));
                if (periodsLate > 0) {
                    totalFine += periodsLate * FINE_PER_PERIOD;
                }
            }
        }
        return totalFine;
    }
} 