package com.perpustakaan.service;

import com.perpustakaan.model.Book;
import com.perpustakaan.model.BorrowTransaction;
import com.perpustakaan.model.CommonUser;
import com.perpustakaan.model.User;
import com.perpustakaan.repository.BookRepository;
import com.perpustakaan.repository.BorrowTransactionRepository;
import com.perpustakaan.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BorrowTransactionServiceTest {

    @Mock
    private BorrowTransactionRepository borrowTransactionRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BorrowTransactionService borrowTransactionService;

    private Book testBook;
    private User testUser;
    private BorrowTransaction testTransaction;

    @BeforeEach
    void setUp() {
        // Setup test book
        testBook = new Book();
        testBook.setIsbn(9786020332956L);
        testBook.setTitle("Test Book");
        testBook.setAuthor("Test Author");
        testBook.setStock(5);

        // Setup test user
        testUser = new CommonUser();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("password");
        testUser.setRole("USER");
        testUser.setFine(0.0);

        // Setup test transaction
        testTransaction = new BorrowTransaction();
        testTransaction.setId(1L);
        testTransaction.setBook(testBook);
        testTransaction.setUser(testUser);
        testTransaction.setStatus("BORROWED");
        testTransaction.setBorrowDate(LocalDateTime.now().minusMinutes(2)); // 2 menit yang lalu
    }

    @Test
    void testSaveBorrowTransactionSuccess() {
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);
        when(borrowTransactionRepository.save(any(BorrowTransaction.class))).thenReturn(testTransaction);

        BorrowTransaction savedTransaction = borrowTransactionService.saveBorrowTransaction(testTransaction);

        assertNotNull(savedTransaction);
        assertEquals("BORROWED", savedTransaction.getStatus());
        assertEquals(4, testBook.getStock()); // stok berkurang 1
        verify(bookRepository).save(testBook);
        verify(borrowTransactionRepository).save(testTransaction);
    }

    @Test
    void testSaveBorrowTransactionNoStock() {
        testBook.setStock(0);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            borrowTransactionService.saveBorrowTransaction(testTransaction);
        });

        assertEquals("Buku tidak tersedia untuk dipinjam", exception.getMessage());
        verify(bookRepository, never()).save(any(Book.class));
        verify(borrowTransactionRepository, never()).save(any(BorrowTransaction.class));
    }

    @Test
    void testReturnBookSuccess() {
        when(borrowTransactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(borrowTransactionRepository.save(any(BorrowTransaction.class))).thenReturn(testTransaction);

        BorrowTransaction returnedTransaction = borrowTransactionService.returnBook(1L);

        assertNotNull(returnedTransaction);
        assertEquals("RETURNED", returnedTransaction.getStatus());
        assertNotNull(returnedTransaction.getReturnDate());
        assertEquals(6, testBook.getStock()); // stok bertambah 1
        verify(bookRepository).save(testBook);
        verify(userRepository).save(testUser);
        verify(borrowTransactionRepository).save(testTransaction);
    }

    @Test
    void testReturnBookNotFound() {
        when(borrowTransactionRepository.findById(999L)).thenReturn(Optional.empty());

        BorrowTransaction result = borrowTransactionService.returnBook(999L);

        assertNull(result);
        verify(bookRepository, never()).save(any(Book.class));
        verify(userRepository, never()).save(any(User.class));
        verify(borrowTransactionRepository, never()).save(any(BorrowTransaction.class));
    }

    @Test
    void testCalculateTransactionFine() {
        testTransaction.setBorrowDate(LocalDateTime.now().minusMinutes(3));

        double fine = borrowTransactionService.calculateTransactionFine(testTransaction);

        // Terlambat 3 menit, denda 10000 per menit
        assertEquals(30000.0, fine); 
    }

    @Test
    void testCalculateTransactionFineTanpaDenda() {
        // Set borrow 30 detik yang lalu, seharusnya tidak ada denda
        testTransaction.setBorrowDate(LocalDateTime.now().minusSeconds(30));

        double fine = borrowTransactionService.calculateTransactionFine(testTransaction);

        assertEquals(0.0, fine);
    }

    @Test
    void testPayFineSuccess() {
        testUser.setFine(10000.0);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(borrowTransactionRepository.findByUserAndStatus(testUser, "BORROWED"))
            .thenReturn(List.of());

        borrowTransactionService.payFine(testUser);

        assertEquals(0.0, testUser.getFine());
        verify(userRepository).save(testUser);
    }

    @Test
    void testPayFineWithActiveBorrowings() {
        testUser.setFine(10000.0);
        when(borrowTransactionRepository.findByUserAndStatus(testUser, "BORROWED"))
            .thenReturn(List.of(testTransaction));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            borrowTransactionService.payFine(testUser);
        });

        assertEquals("Anda harus mengembalikan semua buku yang dipinjam terlebih dahulu sebelum membayar denda", 
            exception.getMessage());
        assertEquals(10000.0, testUser.getFine()); // fine tidak berubah
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testGetAllActiveTransactions() {
        List<BorrowTransaction> expectedTransactions = Arrays.asList(testTransaction);
        when(borrowTransactionRepository.findByStatus("BORROWED", Sort.by(Sort.Direction.DESC, "borrowDate")))
            .thenReturn(expectedTransactions);

        List<BorrowTransaction> actualTransactions = borrowTransactionService.getAllActiveTransactions();

        assertEquals(expectedTransactions.size(), actualTransactions.size());
        assertEquals(expectedTransactions.get(0).getId(), actualTransactions.get(0).getId());
    }

    @Test
    void testGetRecentTransactions() {
        List<BorrowTransaction> expectedTransactions = Arrays.asList(testTransaction);
        when(borrowTransactionRepository.findAll(any(PageRequest.class)))
            .thenReturn(new PageImpl<>(expectedTransactions));

        List<BorrowTransaction> actualTransactions = borrowTransactionService.getRecentTransactions(5);

        assertEquals(expectedTransactions.size(), actualTransactions.size());
        assertEquals(expectedTransactions.get(0).getId(), actualTransactions.get(0).getId());
    }

    @Test
    void testGetActiveBorrowings() {
        List<BorrowTransaction> expectedTransactions = Arrays.asList(testTransaction);
        when(borrowTransactionRepository.findByUserAndStatus(testUser, "BORROWED"))
            .thenReturn(expectedTransactions);

        List<BorrowTransaction> actualTransactions = borrowTransactionService.getActiveBorrowings(testUser);

        assertEquals(expectedTransactions.size(), actualTransactions.size());
        assertEquals(expectedTransactions.get(0).getId(), actualTransactions.get(0).getId());
    }

    @Test
    void testCalculateTotalFine() {
        List<BorrowTransaction> activeTransactions = Arrays.asList(testTransaction);
        when(borrowTransactionRepository.findByUserAndStatus(testUser, "BORROWED"))
            .thenReturn(activeTransactions);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        double totalFine = borrowTransactionService.calculateTotalFine(testUser);

        assertTrue(totalFine > 0); // Should have fine because transaction is overdue
        verify(userRepository).save(testUser);
    }
} 