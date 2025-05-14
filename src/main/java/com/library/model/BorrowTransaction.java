package com.library.model;

import java.time.LocalDateTime;

public class BorrowTransaction {
    private Long id;
    private String status;
    private LocalDateTime borrowDate;
    private LocalDateTime returnDate;

    // Constructors
    public BorrowTransaction() {
    }

    public BorrowTransaction(Long id, String status, LocalDateTime borrowDate, LocalDateTime returnDate) {
        this.id = id;
        this.status = status;
        this.borrowDate = borrowDate;
        this.returnDate = returnDate;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getBorrowDate() {
        return borrowDate;
    }

    public void setBorrowDate(LocalDateTime borrowDate) {
        this.borrowDate = borrowDate;
    }

    public LocalDateTime getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDateTime returnDate) {
        this.returnDate = returnDate;
    }

    // Business methods
    public void borrow() {
        this.status = "BORROWED";
        this.borrowDate = LocalDateTime.now();
        // Default return date is set to 14 days from borrow date
        this.returnDate = this.borrowDate.plusDays(14);
    }

    public void returnBook() {
        this.status = "RETURNED";
        this.returnDate = LocalDateTime.now();
    }
} 