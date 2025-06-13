package com.perpustakaan.model;

import java.time.LocalDateTime;

public interface Borrowable {
    Long getId();
    void setId(Long id);
    
    String getStatus();
    void setStatus(String status);
    
    LocalDateTime getBorrowDate();
    void setBorrowDate(LocalDateTime borrowDate);
    
    LocalDateTime getReturnDate();
    void setReturnDate(LocalDateTime returnDate);
    
    Book getBook();
    void setBook(Book book);
    
    User getUser();
    void setUser(User user);
} 