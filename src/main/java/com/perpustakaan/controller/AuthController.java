package com.perpustakaan.controller;

import com.perpustakaan.model.User;
import com.perpustakaan.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;

@Controller
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }
    
    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }
    
    @PostMapping("/login")
    public String processLogin(@RequestParam String username,
                             @RequestParam String password,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        try {
            User user = authService.login(username, password);
            session.setAttribute("user", user);
            return "redirect:/dashboard";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/login";
        }
    }
    
    @GetMapping("/register")
    public String showRegisterPage() {
        return "register";
    }
    
    @PostMapping("/register")
    public String processRegister(@RequestParam String username,
                                @RequestParam String password,
                                @RequestParam String confirmPassword,
                                RedirectAttributes redirectAttributes) {
        try {
            if (!password.equals(confirmPassword)) {
                throw new RuntimeException("Password tidak cocok");
            }
            
            if (password.length() < 6) {
                throw new RuntimeException("Password minimal 6 karakter");
            }
            
            authService.register(username, password, "USER"); 
            redirectAttributes.addFlashAttribute("success", "Registrasi berhasil! Silakan login.");
            System.out.println("Registrasi berhasil!");
            return "redirect:/login";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }
    
    @PostMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("success", "Anda telah berhasil logout.");
        return "redirect:/login";
    }
} 