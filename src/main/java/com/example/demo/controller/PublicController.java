package com.example.demo.controller;

import com.example.demo.model.Room;
import com.example.demo.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PublicController {

    @Autowired
    private RoomService roomService;

    // Trang chủ công khai cho khách xem phòng có hỗ trợ tìm kiếm và phân trang
    @GetMapping("/")
    public String publicHome(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size, // Hiển thị 6 phòng mỗi trang
            Model model) {
        
        // Sắp xếp theo ID giảm dần (phòng mới thêm hiển thị trước)
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Room> roomPage = roomService.getAvailableRoomsForPublic(keyword, pageable);

        model.addAttribute("roomPage", roomPage);
        model.addAttribute("rooms", roomPage.getContent()); // Tương thích với biến rooms đang dùng ở html
        model.addAttribute("keyword", keyword);
        
        return "public-rooms"; // Trỏ tới file public-rooms.html
    }
}