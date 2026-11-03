package com.example.demo.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) throws Exception {

        // Không tạo Session mới khi người dùng chưa đăng nhập
        HttpSession session = request.getSession(false);

        // Kiểm tra trạng thái đăng nhập
        if (session == null || session.getAttribute("loggedInAdmin") == null) {

            // Chuyển về trang đăng nhập
            response.sendRedirect(request.getContextPath() + "/login");

            return false;
        }

        return true;
    }
}