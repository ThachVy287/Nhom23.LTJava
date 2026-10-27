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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/rooms")
public class RoomController {

    @Autowired
    private RoomService roomService;

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

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("room", new Room());
        return "room-add";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Room room = roomService.getRoomById(id);
        model.addAttribute("room", room);
        return "room-edit";
    }

    @PostMapping("/save")
    public String saveRoom(
        @ModelAttribute("room") Room room, 
        @RequestParam(value = "imageFiles", required = false) MultipartFile[] imageFiles, 
        RedirectAttributes redirectAttributes) {
            try {
                roomService.saveRoomWithImages(room, imageFiles);
                redirectAttributes.addFlashAttribute("successMessage", "Lưu thông tin phòng thành công!");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
                // Nếu thêm mới lỗi -> quay về form add, nếu cập nhật lỗi -> quay về form edit
                if (room.getId() == null) {
                    return "redirect:/admin/rooms/add";
                } else {
                    return "redirect:/admin/rooms/edit/" + room.getId();
                }
            }
            return "redirect:/admin/rooms";
        }

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