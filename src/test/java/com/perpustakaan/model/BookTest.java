package com.perpustakaan.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BookTest {
    
    private Book book;
    
    @BeforeEach
    void setUp() {
        book = new Book();
        book.setIsbn(9786020332956L);
        book.setTitle("Laskar Pelangi");
        book.setAuthor("Andrea Hirata");
        book.setStock(10);
    }
    
    @Test
    void testReduceStockSuccess() {
        book.reduceStock(3);
        assertEquals(7, book.getStock());
    }
    
    @Test
    void testReduceStockInsufficientStock() {
        Exception exception = assertThrows(RuntimeException.class, () -> {
            book.reduceStock(15);
        });
        
        String expectedMessage = "Stok buku tidak mencukupi!";
        String actualMessage = exception.getMessage();
        
        assertEquals(expectedMessage, actualMessage);
        assertEquals(10, book.getStock());
    }
    
    @Test
    void testAddStockSuccess() {
        book.addStock(5);
        assertEquals(15, book.getStock());
    }
    
    @Test
    void testAddStockNegativeAmount() {
        Exception exception = assertThrows(RuntimeException.class, () -> {
            book.addStock(-1);
        });
        
        String expectedMessage = "Jumlah penambahan stok harus lebih dari 0!";
        String actualMessage = exception.getMessage();
        
        assertEquals(expectedMessage, actualMessage);
        System.out.println(actualMessage);
        assertEquals(10, book.getStock());
    }
    
    @Test
    void testGetterAndSetter() {
        Book newBook = new Book();
        
        Long isbn = 9786020332957L;
        String title = "Bumi Manusia";
        String author = "Pramoedya Ananta Toer";
        int stock = 5;
        
        newBook.setIsbn(isbn);
        newBook.setTitle(title);
        newBook.setAuthor(author);
        newBook.setStock(stock);
        
        assertEquals(isbn, newBook.getIsbn());
        assertEquals(title, newBook.getTitle());
        assertEquals(author, newBook.getAuthor());
        assertEquals(stock, newBook.getStock());
    }
} 