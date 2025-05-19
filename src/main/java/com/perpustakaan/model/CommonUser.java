package com.perpustakaan.model;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class CommonUser extends User {
    
    public void borrowBook() {
        // Di implementasikan di borrowTransactionService
    }
    
    public void returnBook() {
        // Di implementasikan di borrowTransactionService
    }
} 