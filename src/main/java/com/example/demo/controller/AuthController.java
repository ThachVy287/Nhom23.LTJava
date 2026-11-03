package com.example.demo.controller;

import jakarta.servlet.http.HttpServletRequest;
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
        return "login";
    }

    // Xử lý đăng nhập
    @PostMapping("/login")
    public String processLogin(
            @RequestParam(defaultValue = "") String username,
            @RequestParam(defaultValue = "") String password,
            HttpServletRequest request,
            Model model) {

        username = username.trim();

        // Kiểm tra thông tin đăng nhập
        if (username.isBlank() || password.isBlank()) {

            model.addAttribute(
                    "error",
                    "Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu!"
            );

            model.addAttribute("username", username);

            return "login";
        }

        // Tài khoản mẫu dùng cho đồ án
        if ("admin".equals(username) &&
                "123456".equals(password)) {

            // Lấy hoặc tạo Session
            HttpSession session = request.getSession();

            // Thay đổi Session ID sau khi đăng nhập thành công
            request.changeSessionId();

            // Lưu thông tin đăng nhập
            session.setAttribute("loggedInAdmin", username);

            return "redirect:/admin";
        }

        // Đăng nhập thất bại
        model.addAttribute(
                "error",
                "Sai tên đăng nhập hoặc mật khẩu!"
        );

        model.addAttribute("username", username);

        return "login";
    }

    // Đăng xuất
    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {

        // Không tạo Session mới khi đăng xuất
        HttpSession session = request.getSession(false);

        if (session != null) {
            session.invalidate();
        }

        return "redirect:/login?logout";
    }
}