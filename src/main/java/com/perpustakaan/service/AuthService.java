package com.perpustakaan.service;

import com.perpustakaan.model.CommonUser;
import com.perpustakaan.model.Admin;
import com.perpustakaan.model.User;
import com.perpustakaan.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

@Service
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    @PostConstruct
    public void init() {
        createDefaultAdmin();
    }
    
    private void createDefaultAdmin() {
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new Admin();
            admin.setUsername("admin");
            admin.setPassword("admin123");
            admin.setRole("ADMIN");
            userRepository.save(admin);
            System.out.println("Default admin account created!");
        }
    }

    public User register(String username, String password, String role) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username sudah digunakan! Silakan pilih username lain.");
        }
        
        User user;
        if ("ADMIN".equals(role)) {
            user = new Admin();
        } else {
            user = new CommonUser();
            role = "USER"; 
        }
        
        user.setUsername(username);
        user.setPassword(password);
        user.setRole(role);
        
        return userRepository.save(user);
    }
    
    public User login(String username, String password) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Username tidak terdaftar! Silakan register terlebih dahulu."));
            
        if (!password.equals(user.getPassword())) {
            throw new RuntimeException("Password yang Anda masukkan salah!");
        }
        
        return user;
    }

    public long getTotalUser() {
        return userRepository.count();
    }
} 