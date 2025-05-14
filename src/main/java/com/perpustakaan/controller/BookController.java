package com.perpustakaan.controller;

import com.perpustakaan.model.Book;
import com.perpustakaan.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/books")
public class BookController {
    
    @Autowired
    private BookService bookService;
    
    @GetMapping
    public String listBooks(Model model) {
        model.addAttribute("books", bookService.getAllBooks());
        return "admin/books/list";
    }
    
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("book", new Book());
        return "admin/books/form";
    }
    
    @PostMapping("/add")
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
    
    @GetMapping("/edit/{isbn}")
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
    
    @PostMapping("/edit/{isbn}")
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
    
    @PostMapping("/delete/{isbn}")
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