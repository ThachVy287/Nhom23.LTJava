package com.example.demo.controller;

import com.example.demo.model.Room;
import com.example.demo.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/rooms")
public class RoomController {

    @Autowired
    private RoomService roomService;

    // 1. Hiển thị trang danh sách phòng trọ
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
        return "rooms";
    }

    // 2. Hiển thị form thêm mới phòng trọ
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("room", new Room());
        return "room-form";
    }

    // 5. MỚI: Hiển thị form cập nhật thông tin phòng trọ theo ID
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Room room = roomService.getRoomById(id); // Lấy thông tin phòng hiện tại
        model.addAttribute("room", room);
        return "room-form"; // Dùng chung file giao diện room-form.html
    }

    // 3. Xử lý lưu phòng trọ (Dùng chung cho cả Thêm mới và Cập nhật)
    @PostMapping("/save")
    public String saveRoom(@ModelAttribute("room") Room room, RedirectAttributes redirectAttributes) {
        roomService.saveRoom(room);
        redirectAttributes.addFlashAttribute("successMessage", "Lưu thông tin phòng thành công!");
        return "redirect:/admin/rooms";
    }

    // 4. Xóa phòng trọ theo ID có bắt lỗi ràng buộc khóa ngoại
    @GetMapping("/delete/{id}")
    public String deleteRoom(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            roomService.deleteRoom(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa phòng thành công!");
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa phòng này vì đang có Hợp đồng, Khách thuê hoặc Hóa đơn liên quan!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi khi xóa phòng!");
        }
        return "redirect:/admin/rooms";
    }
}