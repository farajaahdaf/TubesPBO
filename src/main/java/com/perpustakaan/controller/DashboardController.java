package com.perpustakaan.controller;

import com.perpustakaan.model.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.http.HttpSession;

@Controller
public class DashboardController {
    
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        
        if (user == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("user", user);
        
        if ("ADMIN".equals(user.getRole())) {
            return "admin/dashboard";
        } else {
            return "user/dashboard";
        }
    }
} 