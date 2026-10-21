package com.example.demo.controller;

import com.example.demo.model.Room;
import com.example.demo.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class PublicController {

    @Autowired
    private RoomService roomService;

    // Trang chủ công khai cho khách xem phòng
    @GetMapping("/")
    public String publicHome(Model model) {
        // Lấy tất cả phòng và chỉ lọc ra các phòng còn trống ("TRONG")
        List<Room> availableRooms = roomService.getAllRooms().stream()
                .filter(room -> "TRONG".equals(room.getStatus()))
                .collect(Collectors.toList());

        model.addAttribute("rooms", availableRooms);
        return "public-rooms"; // Trỏ tới file public-rooms.html
    }
}