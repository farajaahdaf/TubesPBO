package com.perpustakaan.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.perpustakaan.model.BorrowTransaction;
import com.perpustakaan.model.User;
import com.perpustakaan.model.Book;
import com.perpustakaan.repository.BorrowTransactionRepository;
import com.perpustakaan.repository.BookRepository;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import com.perpustakaan.repository.UserRepository;


@Service
public class BorrowTransactionService {
    
    @Autowired
    private BorrowTransactionRepository borrowTransactionRepository;
    
    @Autowired
    private BookRepository bookRepository;
    
  
    
    @Autowired
    private UserRepository userRepository;
    
    // Variabel global untuk konfigurasi denda
    public static final int MAX_BORROW_MINUTES = 1; // Maksimal peminjaman 2 menit
    public static final int PERIOD_MINUTES = 1; // Periode denda (1 menit)
    public static final double FINE_PER_PERIOD = 10000.0; // Denda per periode (Rp 10.000)
    
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
                // Hitung denda sebelum mengembalikan buku
                double fine = calculateTransactionFine(transaction);
                if (fine > 0) {
                    // Tambahkan denda ke total denda user
                    user.setFine(user.getFine() + fine);
                    userRepository.save(user);
                }
                
                // Update book stock
                book.setStock(book.getStock() + 1);
                bookRepository.save(book);
                
                // Update transaction status and return date
                transaction.setStatus("RETURNED");
                transaction.setReturnDate(LocalDateTime.now());
                return borrowTransactionRepository.save(transaction);
            }
            return transaction;
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

    @Transactional
    public double calculateTotalFine(User user) {
        List<BorrowTransaction> activeTransactions = borrowTransactionRepository.findByUserAndStatus(user, "BORROWED");
        double totalFine = 0.0;
        
        // Hitung denda untuk peminjaman aktif yang terlambat
        for (BorrowTransaction transaction : activeTransactions) {
            double transactionFine = calculateTransactionFine(transaction);
            totalFine += transactionFine;
        }
        
        // Update denda di database hanya jika berbeda
        if (Math.abs(totalFine - user.getFine()) > 0.01) {
            user.setFine(totalFine);
            userRepository.save(user);
        }
        
        return totalFine;
    }

    public double getCurrentFine(User user) {
        return user.getFine();
    }

    @Transactional
    public void payFine(User user) {
        if (user.getFine() <= 0) {
            throw new RuntimeException("Tidak ada denda yang harus dibayar");
        }
        
        // Cek apakah ada peminjaman aktif
        List<BorrowTransaction> activeLoans = borrowTransactionRepository.findByUserAndStatus(user, "BORROWED");
        if (!activeLoans.isEmpty()) {
            throw new RuntimeException("Anda harus mengembalikan semua buku yang dipinjam terlebih dahulu sebelum membayar denda");
        }
        
        // Reset denda menjadi 0
        user.setFine(0.0);
        // Simpan perubahan ke database
        userRepository.save(user);
    }

    // Tambahkan method untuk menghitung denda per transaksi
    public double calculateTransactionFine(BorrowTransaction transaction) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime borrowDate = transaction.getBorrowDate();
        LocalDateTime dueDate = borrowDate.plusMinutes(MAX_BORROW_MINUTES);
        
        if (now.isAfter(dueDate)) {
            long minutesLate = ChronoUnit.MINUTES.between(dueDate, now);
            // Denda langsung Rp 10.000 saat terlambat
            double fine = FINE_PER_PERIOD;
            // Tambah Rp 10.000 per menit setelah terlambat
            if (minutesLate > 0) {
                fine += minutesLate * FINE_PER_PERIOD;
            }
            return fine;
        }
        return 0.0;
    }

    // Method untuk mendapatkan batas waktu peminjaman
    public static LocalDateTime getDueDate(LocalDateTime borrowDate) {
        return borrowDate.plusMinutes(MAX_BORROW_MINUTES);
    }
} 