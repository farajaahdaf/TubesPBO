package com.perpustakaan.controller;

import com.perpustakaan.model.User;
import com.perpustakaan.model.BorrowTransaction;
import com.perpustakaan.service.AuthService;
import com.perpustakaan.service.BookService;
import com.perpustakaan.service.BorrowTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.http.HttpSession;
import java.util.List;

@Controller
public class DashboardController {
    
    @Autowired
    private BookService bookService;
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private BorrowTransactionService borrowTransactionService;
    
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
        
        // Menambahkan data peminjaman
        long borrowedCount = borrowTransactionService.getActiveBorrowedCount(user);
        long totalTransactions = borrowTransactionService.getTotalTransactions(user);
        
        model.addAttribute("borrowedCount", borrowedCount);
        model.addAttribute("totalTransactions", totalTransactions);
        
        if ("ADMIN".equals(user.getRole())) {
            return "admin/dashboard";
        } else {
            model.addAttribute("books", bookService.getAllBooks());
            // Menambahkan daftar peminjaman aktif
            List<BorrowTransaction> activeLoans = borrowTransactionService.getActiveBorrowings(user);
            model.addAttribute("activeLoans", activeLoans);
            return "user/dashboard";
        }
    }
} 