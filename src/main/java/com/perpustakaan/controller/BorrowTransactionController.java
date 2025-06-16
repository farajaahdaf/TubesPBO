package com.perpustakaan.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.*;
import com.perpustakaan.model.BorrowTransaction;
import com.perpustakaan.model.Book;
import com.perpustakaan.model.User;
import com.perpustakaan.service.BorrowTransactionService;
import com.perpustakaan.service.BookService;
import jakarta.servlet.http.HttpSession;
import java.time.format.DateTimeFormatter;
import java.util.List;
import com.perpustakaan.repository.UserRepository;

@Controller
@RequestMapping("/transactions")
public class BorrowTransactionController {

    @Autowired
    private BorrowTransactionService borrowTransactionService;

    @Autowired
    private BookService bookService;

    @Autowired
    private UserRepository userRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    @GetMapping("/my-borrows")
    public String listTransactions(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        // Hitung total denda
        double fine = borrowTransactionService.calculateTotalFine(user);
        
        // Tambahkan konstanta dan service ke model
        model.addAttribute("MAX_BORROW_MINUTES", BorrowTransactionService.MAX_BORROW_MINUTES);
        model.addAttribute("borrowTransactionService", borrowTransactionService);
        model.addAttribute("user", user);
        model.addAttribute("fine", fine);
        model.addAttribute("activeLoans", borrowTransactionService.getActiveTransactionsByUser(user));
        model.addAttribute("allTransactions", borrowTransactionService.getAllTransactionsByUser(user));
        return "user/my-borrows";
    }

    @PostMapping("/borrow/{isbn}")
    public String borrowBook(@PathVariable Long isbn, HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        try {
            Book book = bookService.getBookByIsbn(isbn);
            // Cek apakah user sudah meminjam buku ini
            List<BorrowTransaction> activeLoans = borrowTransactionService.getActiveBorrowings(user);
            boolean alreadyBorrowed = activeLoans.stream()
                .anyMatch(loan -> loan.getBook().getIsbn().equals(isbn));
            
            if (alreadyBorrowed) {
                redirectAttributes.addFlashAttribute("error", 
                    "Anda sudah meminjam buku ini. Silakan kembalikan terlebih dahulu sebelum meminjam lagi.");
                return "redirect:/dashboard";
            }
            
            if (book != null && book.getStock() > 0) {
                BorrowTransaction transaction = new BorrowTransaction();
                transaction.setBook(book);
                transaction.setUser(user);
                
                BorrowTransaction savedTransaction = borrowTransactionService.saveBorrowTransaction(transaction);
                
                redirectAttributes.addFlashAttribute("success", 
                    "Buku berhasil dipinjam pada " + savedTransaction.getBorrowDate().format(DATE_FORMATTER));
                return "redirect:/transactions/my-borrows";
            }
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        redirectAttributes.addFlashAttribute("error", "Maaf, buku tidak tersedia untuk dipinjam");
        return "redirect:/dashboard";
    }

    @GetMapping("/return/{id}")
    public String returnBook(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        try {
            BorrowTransaction transaction = borrowTransactionService.getTransactionById(id).orElse(null);
            
            if (transaction != null && transaction.getUser().getId().equals(user.getId())) {
                // Hanya mengembalikan buku tanpa menghitung denda
                BorrowTransaction returnedTransaction = borrowTransactionService.returnBook(id);
                
                if (returnedTransaction != null) {
                    redirectAttributes.addFlashAttribute("success", 
                        "Buku berhasil dikembalikan pada " + returnedTransaction.getReturnDate().format(DATE_FORMATTER));
                } else {
                    redirectAttributes.addFlashAttribute("error", "Gagal mengembalikan buku");
                }
            } else {
                redirectAttributes.addFlashAttribute("error", "Transaksi tidak ditemukan atau Anda tidak memiliki akses");
            }
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/transactions/my-borrows";
    }

    @PostMapping("/pay-fine")
    public String payFine(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            try {
                // Cek apakah ada peminjaman aktif
                List<BorrowTransaction> activeLoans = borrowTransactionService.getActiveBorrowings(user);
                if (!activeLoans.isEmpty()) {
                    throw new RuntimeException("Anda harus mengembalikan semua buku yang dipinjam terlebih dahulu sebelum membayar denda");
                }
                
                // Bayar denda
                borrowTransactionService.payFine(user);
                
                // Ambil data user terbaru dari database
                User updatedUser = userRepository.findByUsername(user.getUsername()).orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
                if (updatedUser != null) {
                    // Update session
                    session.setAttribute("user", updatedUser);
                }
                
                redirectAttributes.addFlashAttribute("success", "Pembayaran denda berhasil!");
                return "redirect:/transactions/my-borrows";
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", e.getMessage());
                return "redirect:/transactions/my-borrows";
            }
        }
        return "redirect:/login";
    }

    @PostMapping("/return-with-fine")
    public String returnWithFine(@RequestParam Long transactionId, HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        try {
            BorrowTransaction transaction = borrowTransactionService.getTransactionById(transactionId).orElse(null);
            
            if (transaction != null && transaction.getUser().getId().equals(user.getId())) {
                // Hitung denda sebelum mengembalikan buku
                double fine = borrowTransactionService.calculateTransactionFine(transaction);
                if (fine > 0) {
                    // Tambahkan denda ke total denda user
                    user.setFine(user.getFine() + fine);
                    userRepository.save(user);
                }
                
                // Kembalikan buku
                BorrowTransaction returnedTransaction = borrowTransactionService.returnBook(transactionId);
                
                if (returnedTransaction != null) {
                    redirectAttributes.addFlashAttribute("success", 
                        "Buku berhasil dikembalikan dan denda berhasil dibayar pada " + 
                        returnedTransaction.getReturnDate().format(DATE_FORMATTER));
                } else {
                    redirectAttributes.addFlashAttribute("error", "Gagal mengembalikan buku");
                }
            } else {
                redirectAttributes.addFlashAttribute("error", "Transaksi tidak ditemukan atau Anda tidak memiliki akses");
            }
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/transactions/my-borrows";
    }
} 