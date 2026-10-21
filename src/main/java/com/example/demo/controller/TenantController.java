package com.example.demo.controller;

import com.example.demo.model.Tenant;
import com.example.demo.service.RoomService;
import com.example.demo.service.TenantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/tenants")
public class TenantController {

    @Autowired
    private TenantService tenantService;

    @Autowired
    private RoomService roomService;

    // 1. Hiển thị danh sách khách thuê
    @GetMapping
    public String viewTenants(Model model) {
        model.addAttribute("tenants", tenantService.getAllTenants());
        return "tenants"; // Trỏ tới file tenants.html
    }

    // 2. Hiển thị form thêm mới khách thuê
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("tenant", new Tenant());
        model.addAttribute("rooms", roomService.getAllRooms());
        return "tenant-form"; // Trỏ tới file tenant-form.html
    }

    // 5. Hiển thị form cập nhật (Sửa) thông tin khách thuê theo ID
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Tenant tenant = tenantService.getTenantById(id); // Lấy thông tin khách thuê hiện tại
        model.addAttribute("tenant", tenant);
        model.addAttribute("rooms", roomService.getAllRooms()); // Danh sách phòng để đổi/chọn lại
        return "tenant-form"; // Dùng chung file tenant-form.html
    }

    // 3. Xử lý lưu khách thuê (Dùng chung cho cả Thêm mới và Cập nhật)
    @PostMapping("/save")
    public String saveTenant(@ModelAttribute("tenant") Tenant tenant, RedirectAttributes redirectAttributes) {
        tenantService.saveTenant(tenant);
        redirectAttributes.addFlashAttribute("successMessage", "Lưu thông tin khách thuê thành công!");
        return "redirect:/tenants";
    }

    // 4. Xóa khách thuê có bắt lỗi ràng buộc khóa ngoại
    @GetMapping("/delete/{id}")
    public String deleteTenant(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            tenantService.deleteTenant(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa khách thuê thành công!");
        } catch (DataIntegrityViolationException e) {
            // Bắt lỗi khi khách đang dính khóa ngoại ở bảng Hợp Đồng (contracts)
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa khách thuê này vì đang có Hợp Đồng thuê phòng liên quan! Vui lòng thanh lý hợp đồng trước.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi khi xóa khách thuê!");
        }
        return "redirect:/tenants";
    }
}