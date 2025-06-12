package com.perpustakaan.service;

import com.perpustakaan.model.User;

public interface UserService {
    User findByUsername(String username);
    User findById(Long id);
    User save(User user);
    void updateFine(User user, double fine);
} 