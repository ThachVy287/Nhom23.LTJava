package com.example.demo.controller;

import com.example.demo.model.Tenant;
import com.example.demo.service.RoomService;
import com.example.demo.service.TenantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/tenants")
public class TenantController {

    @Autowired
    private TenantService tenantService;

    @Autowired
    private RoomService roomService; // Dùng để lấy danh sách phòng gán cho khách

    // Hiển thị danh sách khách thuê
    @GetMapping
    public String viewTenants(Model model) {
        model.addAttribute("tenants", tenantService.getAllTenants());
        return "tenants"; // Trỏ tới file tenants.html
    }

    // Form thêm khách thuê mới
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("tenant", new Tenant());
        model.addAttribute("rooms", roomService.getAllRooms()); // Truyền danh sách phòng sang form chọn
        return "tenant-form"; // Trỏ tới file tenant-form.html
    }

    // Lưu khách thuê
    @PostMapping("/save")
    public String saveTenant(@ModelAttribute("tenant") Tenant tenant) {
        tenantService.saveTenant(tenant);
        return "redirect:/tenants";
    }

    // Xóa khách thuê
    @GetMapping("/delete/{id}")
    public String deleteTenant(@PathVariable Long id) {
        tenantService.deleteTenant(id);
        return "redirect:/tenants";
    }
}