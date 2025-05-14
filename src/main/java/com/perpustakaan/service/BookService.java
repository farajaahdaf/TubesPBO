package com.perpustakaan.service;

import com.perpustakaan.model.Book;
import com.perpustakaan.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BookService {
    
    @Autowired
    private BookRepository bookRepository;
    
    public Book addBook(Book book) {
        if (bookRepository.existsById(book.getIsbn())) {
            throw new RuntimeException("Buku dengan ISBN " + book.getIsbn() + " sudah ada!");
        }
        return bookRepository.save(book);
    }
    
    public Book editBook(Book book) {
        if (!bookRepository.existsById(book.getIsbn())) {
            throw new RuntimeException("Buku dengan ISBN " + book.getIsbn() + " tidak ditemukan!");
        }
        return bookRepository.save(book);
    }
    
    public void deleteBook(Long isbn) {
        if (!bookRepository.existsById(isbn)) {
            throw new RuntimeException("Buku dengan ISBN " + isbn + "tidak ditemukan!");
        }
        bookRepository.deleteById(isbn);
    }
    
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }
    
    public Book getBookByIsbn(Long isbn) {
        return bookRepository.findById(isbn)
            .orElseThrow(() -> new RuntimeException("Buku dengan ISBN " + isbn + "tidak ditemukan!"));
    }
    
    public void updateStock(Long isbn, int amount, boolean isAdd) {
        Book book = getBookByIsbn(isbn);
        if (isAdd) {
            book.addStock(amount);
        } else {
            book.reduceStock(amount);
        }
        bookRepository.save(book);
    }

    public long getTotalBooks() {
        return bookRepository.count();
    }

    public int getTotalBookStock() {
        return getAllBooks().stream()
                .mapToInt(Book::getStock)
                .sum();
    }

    public List<Book> searchBooks(String keyword) {
        return bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(keyword, keyword);
    }
} 