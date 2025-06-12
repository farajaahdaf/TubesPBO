package com.perpustakaan.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
public abstract class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(nullable = false)
    private String password;
    
    @Column(nullable = false)
    private String role;
    
    @Column(nullable = false)
    private double fine = 0.0; // Denda user, default 0
    
    public boolean login(String username, String password) {
        return this.username.equals(username) && this.password.equals(password);
    }
    
    public void logout() {
        return;
    }
} 