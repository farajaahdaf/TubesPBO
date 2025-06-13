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
import org.springframework.web.bind.annotation.PostMapping;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import java.time.LocalDateTime;
import org.springframework.web.bind.annotation.ResponseBody;

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
        
        // Ambil data user terbaru dari database
        User currentUser = authService.getUserById(user.getId());
        session.setAttribute("user", currentUser);
        
        model.addAttribute("user", currentUser);
        model.addAttribute("JumlahUser", authService.getTotalUser());
        model.addAttribute("totalBooks", bookService.getTotalBooks());
        model.addAttribute("totalBookStock", bookService.getTotalBookStock());
        
        if ("ADMIN".equals(currentUser.getRole())) {
            // Get all active loans for admin dashboards
            List<BorrowTransaction> activeLoans = borrowTransactionService.getAllActiveTransactions();
            model.addAttribute("activeLoans", activeLoans);
            model.addAttribute("activeLoanCount", activeLoans.size());
            
            // Get recent activities (last 10 transactions)
            List<BorrowTransaction> recentActivities = borrowTransactionService.getRecentTransactions(10);
            model.addAttribute("recentActivities", recentActivities);
            
            // Get count of overdue books
            long overdueCount = borrowTransactionService.getOverdueTransactionsCount();
            model.addAttribute("overdueCount", overdueCount);
            
            // Get all users with their fines
            List<User> allUsers = authService.getAllUsers();
            // Update fines for all users
            allUsers.forEach(u -> {
                if (!"ADMIN".equals(u.getRole())) {
                    double totalFine = borrowTransactionService.calculateTotalFine(u);
                    u.setFine(totalFine);
                }
            });
            model.addAttribute("allUsers", allUsers);
            
            return "admin/dashboard";
        } else {
            // Get all books and active loans
            List<Book> allBooks = bookService.getAllBooks();
            List<BorrowTransaction> activeLoans = borrowTransactionService.getActiveBorrowings(currentUser);
            
            // Create a list of ISBNs of currently borrowed books
            List<Long> borrowedBookIsbn = activeLoans.stream()
                .map(loan -> loan.getBook().getIsbn())
                .toList();
            
            model.addAttribute("books", allBooks);
            model.addAttribute("borrowedBookIsbn", borrowedBookIsbn);
            model.addAttribute("activeLoans", activeLoans);
            model.addAttribute("borrowedCount", activeLoans.size());
            model.addAttribute("totalTransactions", borrowTransactionService.getTotalTransactions(currentUser));
            
            // Update denda secara realtime
            double totalFine = borrowTransactionService.calculateTotalFine(currentUser);
            model.addAttribute("totalFine", totalFine);
            
            // Hitung total keterlambatan
            long overdueCount = activeLoans.stream()
                .filter(loan -> {
                    LocalDateTime now = LocalDateTime.now();
                    LocalDateTime dueDate = loan.getBorrowDate().plusMinutes(2);
                    return now.isAfter(dueDate);
                })
                .count();
            model.addAttribute("overdueCount", overdueCount);
            
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

    @PostMapping("/pay-fine")
    public String payFine(HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        try {
            // Cek apakah ada peminjaman aktif
            List<BorrowTransaction> activeLoans = borrowTransactionService.getActiveBorrowings(user);
            if (!activeLoans.isEmpty()) {
                throw new RuntimeException("Anda harus mengembalikan semua buku yang dipinjam terlebih dahulu sebelum membayar denda");
            }
            
            // Reset denda menjadi 0
            user.setFine(0.0);
            authService.updateUser(user);
            
            // Update session
            session.setAttribute("user", user);
            
            redirectAttributes.addFlashAttribute("success", "Denda berhasil dibayar!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/dashboard";
    }
} 