package com.perpustakaan.controller;

import com.perpustakaan.model.User;
import com.perpustakaan.model.BorrowTransaction;
import com.perpustakaan.model.Book;
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
        
        if ("ADMIN".equals(user.getRole())) {
            // Get all active loans for admin dashboard
            List<BorrowTransaction> activeLoans = borrowTransactionService.getAllActiveTransactions();
            model.addAttribute("activeLoans", activeLoans);
            model.addAttribute("activeLoanCount", activeLoans.size());
            
            // Get recent activities (last 10 transactions)
            List<BorrowTransaction> recentActivities = borrowTransactionService.getRecentTransactions(10);
            model.addAttribute("recentActivities", recentActivities);
            
            // Get count of overdue books
            long overdueCount = borrowTransactionService.getOverdueTransactionsCount();
            model.addAttribute("overdueCount", overdueCount);
            
            return "admin/dashboard";
        } else {
            // Get all books and active loans
            List<Book> allBooks = bookService.getAllBooks();
            List<BorrowTransaction> activeLoans = borrowTransactionService.getActiveBorrowings(user);
            
            // Create a list of ISBNs of currently borrowed books
            List<Long> borrowedBookIsbn = activeLoans.stream()
                .map(loan -> loan.getBook().getIsbn())
                .toList();
            
            model.addAttribute("books", allBooks);
            model.addAttribute("borrowedBookIsbn", borrowedBookIsbn);
            model.addAttribute("activeLoans", activeLoans);
            model.addAttribute("borrowedCount", activeLoans.size());
            model.addAttribute("totalTransactions", borrowTransactionService.getTotalTransactions(user));
            return "user/dashboard";
        }
    }

    @GetMapping("/admin/users")
    public String listUsers(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        
        if (user == null || !"ADMIN".equals(user.getRole())) {
            return "redirect:/login";
        }
        
        List<User> users = authService.getAllUsers();
        model.addAttribute("users", users);
        
        return "admin/listUser";
    }
} 