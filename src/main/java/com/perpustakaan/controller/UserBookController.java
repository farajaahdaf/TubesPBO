package com.perpustakaan.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.perpustakaan.service.BookService;
import com.perpustakaan.model.User;
import jakarta.servlet.http.HttpSession;

@Controller
public class UserBookController {

    @Autowired
    private BookService bookService;

    @GetMapping("/books-list")
    public String booksList(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", user);
        model.addAttribute("books", bookService.getAllBooks());
        
        return "user/books-list";
    }
} 