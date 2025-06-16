package com.perpustakaan.service;

import com.perpustakaan.model.CommonUser;
import com.perpustakaan.model.User;
import com.perpustakaan.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthService authService;

    private CommonUser testUser;

    @BeforeEach
    void setUp() {
        testUser = new CommonUser();
        testUser.setId(1L);
        testUser.setUsername("johndoe");
        testUser.setPassword("password123");
        testUser.setRole("USER");
    }

    @Test
    void testRegisterSuccess() {
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(3L);
            return user;
        });

        User registeredUser = authService.register("newuser", "pass123", "USER");

        assertNotNull(registeredUser);
        assertEquals("newuser", registeredUser.getUsername());
        assertEquals("pass123", registeredUser.getPassword());
        assertEquals("USER", registeredUser.getRole());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testRegisterDuplicateUsername() {
        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(testUser));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            authService.register("johndoe", "password123", "USER");
        });

        assertEquals("Username sudah digunakan! Silakan pilih username lain.", exception.getMessage());
        System.out.println(exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testLoginSuccess() {
        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(testUser));

        User loggedInUser = authService.login("johndoe", "password123");

        assertNotNull(loggedInUser);
        assertEquals("johndoe", loggedInUser.getUsername());
        assertEquals("USER", loggedInUser.getRole());
    }

    @Test
    void testLoginWrongPassword() {
        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(testUser));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            authService.login("johndoe", "wrongpassword");
        });

        assertEquals("Password yang Anda masukkan salah!", exception.getMessage());
    }

    @Test
    void testLoginUserNotFound() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            authService.login("nonexistent", "password123");
        });

        assertEquals("Username tidak terdaftar! Silakan register terlebih dahulu.", exception.getMessage());
    }
} 