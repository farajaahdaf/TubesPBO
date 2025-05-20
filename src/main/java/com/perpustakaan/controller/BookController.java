package com.perpustakaan.controller;

import com.perpustakaan.model.Book;
import com.perpustakaan.model.User;
import com.perpustakaan.model.BorrowTransaction;
import com.perpustakaan.service.BookService;
import com.perpustakaan.service.BorrowTransactionService;
import com.perpustakaan.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.time.LocalDateTime;

@Controller
public class BookController {
    
    @Autowired
    private BookService bookService;
    
    @Autowired
    private BorrowTransactionService borrowTransactionService;
    
    @Autowired
    private AuthService authService;
    
    // Endpoint untuk user melihat daftar buku
    @GetMapping("/books-list")
    public String userListBooks(@RequestParam(value = "keyword", required = false) String keyword, Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        // melihat yang lagi dipinjam
        List<BorrowTransaction> activeLoans = borrowTransactionService.getActiveBorrowings(user);
        
        
        List<Long> borrowedBookIsbn = activeLoans.stream()
            .map(loan -> loan.getBook().getIsbn())
            .toList();

        model.addAttribute("user", user);
        model.addAttribute("borrowedBookIsbn", borrowedBookIsbn);
        if (keyword != null && !keyword.isEmpty()) {
            model.addAttribute("books", bookService.searchBooks(keyword));
        } else {
            model.addAttribute("books", bookService.getAllBooks());
        }
        return "user/books-list";
    }
    public String userBooksList(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        return "user/books-list";
    }



    // Endpoint untuk melihat peminjaman user
    @GetMapping("/my-borrows")
    public String myBorrows(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", user);
        model.addAttribute("activeLoans", borrowTransactionService.getActiveBorrowings(user));
        model.addAttribute("allTransactions", borrowTransactionService.getTransactionsByUser(user));
        model.addAttribute("fine", borrowTransactionService.calculateTotalFine(user));
        
        return "user/my-borrows";
    }
    
    // Endpoint untuk admin
    @GetMapping("/admin/books")
    public String adminListBooks(@RequestParam(value = "keyword", required = false) String keyword, Model model) {
        if (keyword != null && !keyword.isEmpty()) {
            model.addAttribute("books", bookService.searchBooks(keyword));
        } else {
            model.addAttribute("books", bookService.getAllBooks());
        }
        return "admin/books/list";
    }
    
    @GetMapping("/admin/books/add")
    public String showAddForm(Model model) {
        model.addAttribute("book", new Book());
        return "admin/books/form";
    }
    
    @PostMapping("/admin/books/add")
    public String addBook(@ModelAttribute Book book, RedirectAttributes redirectAttributes) {
        try {
            bookService.addBook(book);
            redirectAttributes.addFlashAttribute("success", "Buku berhasil ditambahkan!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/books/add";
        }
        return "redirect:/admin/books";
    }
    
    @GetMapping("/admin/books/edit/{isbn}")
    public String showEditForm(@PathVariable Long isbn, Model model, RedirectAttributes redirectAttributes) {
        try {
            Book book = bookService.getBookByIsbn(isbn);
            model.addAttribute("book", book);
            return "admin/books/form";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/books";
        }
    }
    
    @PostMapping("/admin/books/edit/{isbn}")
    public String editBook(@PathVariable Long isbn, @ModelAttribute Book book, RedirectAttributes redirectAttributes) {
        try {
            book.setIsbn(isbn);
            bookService.editBook(book);
            redirectAttributes.addFlashAttribute("success", "Buku berhasil diperbarui!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/books/edit/" + isbn;
        }
        return "redirect:/admin/books";
    }
    
    @PostMapping("/admin/books/delete/{isbn}")
    public String deleteBook(@PathVariable Long isbn, RedirectAttributes redirectAttributes) {
        try {
            bookService.deleteBook(isbn);
            redirectAttributes.addFlashAttribute("success", "Buku berhasil dihapus!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/books";
    }
} 