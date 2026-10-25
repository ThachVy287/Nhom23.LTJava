package com.example.demo.controller;

import com.example.demo.model.Room;
import com.example.demo.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class HomeController {

    @Autowired
    private RoomService roomService;

    // Hiển thị trang chủ công khai các phòng đang trống
    @GetMapping("/public")
    public String viewHomePage(Model model) {
        model.addAttribute("rooms", roomService.getAllRooms().stream()
                .filter(r -> "TRONG".equals(r.getStatus()))
                .toList());
        return "index"; // Trỏ tới file index.html (hoặc file giao diện trang chủ của bạn)
    }

    // Hiển thị trang chi tiết phòng (Đường dẫn đúng: /rooms/detail/{id})
    @GetMapping("/rooms/detail/{id}")
    public String viewRoomDetail(@PathVariable Long id, Model model) {
        Room room = roomService.getRoomById(id);
        model.addAttribute("room", room);
        return "room-detail"; // Trỏ tới file room-detail.html
    }
}