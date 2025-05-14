package com.perpustakaan.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.perpustakaan.service.BorrowTransactionService;
import com.perpustakaan.model.User;
import jakarta.servlet.http.HttpSession;

@Controller
public class UserBorrowController {

    @Autowired
    private BorrowTransactionService borrowTransactionService;

    @GetMapping("/my-borrows")
    public String myBorrows(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", user);
        model.addAttribute("activeLoans", borrowTransactionService.getActiveBorrowings(user));
        model.addAttribute("allTransactions", borrowTransactionService.getTransactionsByUser(user));
        
        return "user/my-borrows";
    }
} 