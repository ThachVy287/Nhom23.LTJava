package com.example.demo.config;

import com.example.demo.interceptor.AdminInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private AdminInterceptor adminInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Chặn toàn bộ các đường dẫn bắt đầu bằng /admin/ ngoại trừ trang chủ admin tổng quan nếu cần
        registry.addInterceptor(adminInterceptor)
                .addPathPatterns("/admin/**"); 
    }
}