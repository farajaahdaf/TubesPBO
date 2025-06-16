package com.perpustakaan.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserTest {
    
    private CommonUser user;
    
    @BeforeEach
    void setUp() {
        user = new CommonUser();
        user.setId(1L);
        user.setUsername("johndoe");
        user.setPassword("password123");
        user.setRole("USER");
        user.setFine(0.0);
    }
    
    @Test
    void testLoginSuccess() {
        assertTrue(user.login("johndoe", "password123"));
    }
    
    @Test
    void testLoginFailWrongPassword() {
        assertFalse(user.login("johndoe", "wrongpassword"));
    }
    
    @Test
    void testLoginFailWrongUsername() {
        assertFalse(user.login("wronguser", "password123"));
    }
    
    @Test
    void testGetterAndSetter() {
        CommonUser newUser = new CommonUser();
        
        Long id = 2L;
        String username = "janedoe";
        String password = "pass456";
        String role = "USER";
        double fine = 5000.0;
        
        newUser.setId(id);
        newUser.setUsername(username);
        newUser.setPassword(password);
        newUser.setRole(role);
        newUser.setFine(fine);
        
        assertEquals(id, newUser.getId());
        assertEquals(username, newUser.getUsername());
        assertEquals(password, newUser.getPassword());
        assertEquals(role, newUser.getRole());
        assertEquals(fine, newUser.getFine());
    }
    
    @Test
    void testInitialFineIsZero() {
        CommonUser newUser = new CommonUser();
        assertEquals(0.0, newUser.getFine());
    }
    
    @Test
    void testUpdateFine() {
        double newFine = 10000.0;
        user.setFine(newFine);
        assertEquals(newFine, user.getFine());
    }
} 