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

@Controller
@RequestMapping("/transactions")
public class BorrowTransactionController {

    @Autowired
    private BorrowTransactionService borrowTransactionService;

    @Autowired
    private BookService bookService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    @GetMapping("/my-borrows")
    public String listTransactions(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("user", user);
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

        Book book = bookService.getBookByIsbn(isbn);
        
        if (book != null && book.getStock() > 0) {
            BorrowTransaction transaction = new BorrowTransaction();
            transaction.setBook(book);
            transaction.setUser(user);
            transaction.borrow();
            borrowTransactionService.saveBorrowTransaction(transaction);
            
            redirectAttributes.addFlashAttribute("success", 
                "Buku berhasil dipinjam pada " + transaction.getBorrowDate().format(DATE_FORMATTER));
            return "redirect:/transactions/my-borrows";
        }
        
        redirectAttributes.addFlashAttribute("error", "Maaf, buku tidak tersedia untuk dipinjam");
        return "redirect:/books-list";
    }

    @GetMapping("/return/{id}")
    public String returnBook(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        BorrowTransaction transaction = borrowTransactionService.getTransactionById(id).orElse(null);
        
        if (transaction != null && transaction.getUser().getId().equals(user.getId())) {
            transaction.returnBook();
            borrowTransactionService.saveBorrowTransaction(transaction);
            
            redirectAttributes.addFlashAttribute("success", 
                "Buku berhasil dikembalikan pada " + transaction.getReturnDate().format(DATE_FORMATTER));
        } else {
            redirectAttributes.addFlashAttribute("error", "Gagal mengembalikan buku");
        }
        
        return "redirect:/transactions/my-borrows";
    }
} 