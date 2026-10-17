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
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/rooms")
public class RoomController {

    @Autowired
    private RoomService roomService;

    // 1. Hiển thị trang danh sách phòng trọ (có tìm kiếm, lọc, phân trang)
    // URL truy cập: http://localhost:8080/rooms
    @GetMapping
    public String viewRooms(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Room> roomPage = roomService.getRooms(keyword, status, pageable);

        model.addAttribute("roomPage", roomPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        return "rooms"; // Trỏ tới file templates/rooms.html
    }

    // 2. Hiển thị form thêm mới phòng trọ
    // URL: http://localhost:8080/rooms/add
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("room", new Room());
        return "room-form"; // Trỏ tới file templates/room-form.html
    }

    // 3. Xử lý lưu phòng trọ (Thêm mới hoặc Cập nhật)
    @PostMapping("/save")
    public String saveRoom(@ModelAttribute("room") Room room) {
        roomService.saveRoom(room);
        return "redirect:/rooms"; // Lưu xong quay về trang danh sách
    }

    // 4. Xóa phòng trọ theo ID
    @GetMapping("/delete/{id}")
    public String deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return "redirect:/rooms"; // Xóa xong quay về trang danh sách
    }
}