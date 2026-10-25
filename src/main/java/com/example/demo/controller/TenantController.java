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

    // 2. Hiển thị form THÊM MỚI khách thuê
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("tenant", new Tenant());
        model.addAttribute("rooms", roomService.getAllRooms());
        return "tenant-add"; // Trỏ tới file tenant-add.html
    }

    // 3. Hiển thị form CẬP NHẬT khách thuê theo ID
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Tenant tenant = tenantService.getTenantById(id);
        model.addAttribute("tenant", tenant);
        model.addAttribute("rooms", roomService.getAllRooms());
        return "tenant-edit"; // Trỏ tới file tenant-edit.html
    }

    // 4. Xử lý lưu khách thuê chung (cho cả thêm mới và cập nhật)
    @PostMapping("/save")
    public String saveTenant(@ModelAttribute("tenant") Tenant tenant, RedirectAttributes redirectAttributes) {
        tenantService.saveTenant(tenant);
        redirectAttributes.addFlashAttribute("successMessage", "Lưu thông tin khách thuê thành công!");
        return "redirect:/tenants";
    }

    // 5. Xóa khách thuê có bắt lỗi ràng buộc khóa ngoại
    @GetMapping("/delete/{id}")
    public String deleteTenant(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            tenantService.deleteTenant(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa khách thuê thành công!");
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa khách thuê này vì đang có Hợp Đồng thuê phòng liên quan! Vui lòng thanh lý hợp đồng trước.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi khi xóa khách thuê!");
        }
        return "redirect:/tenants";
    }
}