package com.perpustakaan.model;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class CommonUser extends User {
    
    public void borrowBook() {
        // Implementasi akan ditambahkan nanti
    }
    
    public void returnBook() {
        // Implementasi akan ditambahkan nanti
    }
} 