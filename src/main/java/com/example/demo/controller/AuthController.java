package com.example.demo.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    // Hiển thị trang đăng nhập
    @GetMapping("/login")
    public String showLoginForm() {
        return "login"; // Trỏ tới file login.html
    }

    // Xử lý xác thực đăng nhập
    @PostMapping("/login")
    public String processLogin(@RequestParam String username, 
                             @RequestParam String password, 
                             HttpSession session, 
                             Model model) {
        // Tài khoản và mật khẩu mẫu cho chủ trọ
        if ("admin".equals(username) && "123456".equals(password)) {
            session.setAttribute("loggedInAdmin", username); // Lưu phiên đăng nhập
            return "redirect:/admin"; // Đăng nhập thành công vào trang tổng quan admin
        } else {
            model.addAttribute("error", "Sai tên đăng nhập hoặc mật khẩu!");
            return "login";
        }
    }

    // Đăng xuất
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // Xóa phiên đăng nhập
        return "redirect:/login?logout";
    }
}