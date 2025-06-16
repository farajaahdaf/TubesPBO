package com.perpustakaan.service;

import com.perpustakaan.model.Book;
import com.perpustakaan.model.BorrowTransaction;
import com.perpustakaan.repository.BookRepository;
import com.perpustakaan.repository.BorrowTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BorrowTransactionRepository borrowTransactionRepository;

    @InjectMocks
    private BookService bookService;

    private Book testBook;

    @BeforeEach
    void setUp() {
        testBook = new Book();
        testBook.setIsbn(9786020332956L);
        testBook.setTitle("Laskar Pelangi");
        testBook.setAuthor("Andrea Hirata");
        testBook.setStock(10);
    }

    @Test
    void testAddBookSuccess() {
        when(bookRepository.existsById(testBook.getIsbn())).thenReturn(false);
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);

        Book savedBook = bookService.addBook(testBook);

        assertNotNull(savedBook);
        assertEquals(testBook.getIsbn(), savedBook.getIsbn());
        verify(bookRepository).save(testBook);
    }

    @Test
    void testAddBookDuplicate() {
        when(bookRepository.existsById(testBook.getIsbn())).thenReturn(true);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            bookService.addBook(testBook);
        });

        assertEquals("Buku dengan ISBN " + testBook.getIsbn() + " sudah ada!", exception.getMessage());
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void testDeleteBookSuccess() {
        when(bookRepository.existsById(testBook.getIsbn())).thenReturn(true);
        when(borrowTransactionRepository.findByBookIsbnAndStatus(testBook.getIsbn(), "BORROWED"))
            .thenReturn(new ArrayList<>());

        bookService.deleteBook(testBook.getIsbn());

        verify(borrowTransactionRepository).deleteByBookIsbn(testBook.getIsbn());
        verify(bookRepository).deleteById(testBook.getIsbn());
    }

    @Test
    void testDeleteBookWithActiveBorrows() {
        when(bookRepository.existsById(testBook.getIsbn())).thenReturn(true);
        List<BorrowTransaction> activeLoans = List.of(new BorrowTransaction());
        when(borrowTransactionRepository.findByBookIsbnAndStatus(testBook.getIsbn(), "BORROWED"))
            .thenReturn(activeLoans);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            bookService.deleteBook(testBook.getIsbn());
        });

        assertEquals("Buku tidak dapat dihapus karena sedang dipinjam oleh 1 user!", exception.getMessage());
        verify(borrowTransactionRepository, never()).deleteByBookIsbn(any());
        verify(bookRepository, never()).deleteById(any());
    }

    @Test
    void testGetAllBooks() {
        List<Book> expectedBooks = List.of(testBook);
        when(bookRepository.findAll()).thenReturn(expectedBooks);

        List<Book> actualBooks = bookService.getAllBooks();

        assertEquals(expectedBooks.size(), actualBooks.size());
        assertEquals(expectedBooks.get(0).getIsbn(), actualBooks.get(0).getIsbn());
    }

    @Test
    void testUpdateStockAdd() {
        when(bookRepository.findById(testBook.getIsbn())).thenReturn(Optional.of(testBook));
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);

        bookService.updateStock(testBook.getIsbn(), 5, true);

        assertEquals(15, testBook.getStock());
        verify(bookRepository).save(testBook);
    }

    @Test
    void testUpdateStockReduce() {
        when(bookRepository.findById(testBook.getIsbn())).thenReturn(Optional.of(testBook));
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);

        bookService.updateStock(testBook.getIsbn(), 3, false);

        assertEquals(7, testBook.getStock());
        verify(bookRepository).save(testBook);
    }
} 