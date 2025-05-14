package com.perpustakaan.model;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Admin extends User {
    
    public void addBook(Book book) {
        // Method ini akan diimplementasikan di BookService
        // Admin hanya memiliki hak akses untuk menambah buku
    }
    
    public void editBook(Book book) {
        // Method ini akan diimplementasikan di BookService
        // Admin hanya memiliki hak akses untuk mengedit buku
    }
    
    public void deleteBook(Long isbn) {
        // Method ini akan diimplementasikan di BookService
        // Admin hanya memiliki hak akses untuk menghapus buku
    }
} 