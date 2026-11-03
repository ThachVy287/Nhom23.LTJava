
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

import java.io.IOException;

@Controller
@RequestMapping("/admin/rooms")
public class RoomController {

    private static final String AVAILABLE = "TRONG";
    private static final String RENTED = "DA_THUE";

    @Autowired
    private RoomService roomService;

    // Danh sách phòng
    @GetMapping
    public String viewRooms(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model) {

        int safePage = Math.max(page, 0);

        int safeSize;
        if (size == 10 || size == 20 || size == 50) {
            safeSize = size;
        } else {
            safeSize = 5;
        }

        if (!AVAILABLE.equals(status) && !RENTED.equals(status)) {
            status = null;
        }

        Pageable pageable = PageRequest.of(
                safePage,
                safeSize,
                Sort.by("id").descending()
        );

        Page<Room> roomPage = roomService.getRooms(
                keyword, status, pageable);

        // Nếu trang hiện tại không còn dữ liệu, chuyển về trang cuối
        if (roomPage.getTotalPages() > 0 &&
                safePage >= roomPage.getTotalPages()) {

            safePage = roomPage.getTotalPages() - 1;

            pageable = PageRequest.of(
                    safePage,
                    safeSize,
                    Sort.by("id").descending()
            );

            roomPage = roomService.getRooms(
                    keyword, status, pageable);
        }

        model.addAttribute("roomPage", roomPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("pageSize", safeSize);

        return "rooms";
    }

    // Hiển thị form thêm phòng
    @GetMapping("/add")
    public String showAddForm(Model model) {

        model.addAttribute("room", new Room());

        return "room-add";
    }

    // Hiển thị form sửa phòng
    @GetMapping("/edit/{id}")
    public String showEditForm(
            @PathVariable Long id,
            Model model) {

        Room room = roomService.getRoomById(id);

        model.addAttribute("room", room);

        return "room-edit";
    }

    // Lưu phòng
    @PostMapping("/save")
    public String saveRoom(
            @ModelAttribute("room") Room room,
            @RequestParam(value = "imageFiles", required = false)
            MultipartFile[] imageFiles,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {

            roomService.saveRoomWithImages(room, imageFiles);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Lưu thông tin phòng thành công!"
            );

            return "redirect:/admin/rooms";

        } catch (DataIntegrityViolationException e) {

            model.addAttribute(
                    "errorMessage",
                    "Không thể lưu phòng vì số phòng bị trùng " +
                    "hoặc dữ liệu đang được sử dụng!"
            );

        } catch (RuntimeException e) {

            model.addAttribute(
                    "errorMessage",
                    e.getMessage() != null
                            ? e.getMessage()
                            : "Không thể lưu thông tin phòng!"
            );

        } catch (IOException e) {

            model.addAttribute(
                    "errorMessage",
                    "Không thể tải hình ảnh lên máy chủ. " +
                    "Vui lòng kiểm tra lại và thử lại!"
            );
        }

        // Trả trực tiếp về form để giữ dữ liệu đã nhập
        if (room.getId() == null) {
            return "room-add";
        }

        return "room-edit";
    }

    // Xóa phòng bằng POST
    @PostMapping("/delete/{id}")
    public String deleteRoom(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {

            roomService.deleteRoom(id);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Xóa phòng thành công!"
            );

        } catch (RuntimeException e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage() != null
                            ? e.getMessage()
                            : "Không thể xóa phòng!"
            );

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Đã xảy ra lỗi khi xóa phòng!"
            );
        }

        return "redirect:/admin/rooms";
    }
}
