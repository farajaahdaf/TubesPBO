package com.perpustakaan.controller;

import com.perpustakaan.model.User;
import com.perpustakaan.service.AuthService;
import com.perpustakaan.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.http.HttpSession;

@Controller
public class DashboardController {
    
    @Autowired
    private BookService bookService;
    
    @Autowired
    private AuthService authService;
    
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        
        if (user == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("user", user);
        model.addAttribute("JumlahUser", authService.getTotalUser());
        model.addAttribute("totalBooks", bookService.getTotalBooks());
        model.addAttribute("totalBookStock", bookService.getTotalBookStock());
        
        if ("ADMIN".equals(user.getRole())) {
            return "admin/dashboard";
        } else {
            model.addAttribute("books", bookService.getAllBooks());
            return "user/dashboard";
        }
    }
} 