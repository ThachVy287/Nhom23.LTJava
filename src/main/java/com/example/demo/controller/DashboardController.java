package com.example.demo.controller;

import com.example.demo.service.RoomService;
import com.example.demo.service.TenantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @Autowired
    private RoomService roomService;

    @Autowired
    private TenantService tenantService;

    @GetMapping("/")
    public String dashboard(Model model) {
        long totalRooms = roomService.getAllRooms().size();
        long totalTenants = tenantService.getAllTenants().size();
        
        // Đếm số phòng trống và đã thuê
        long emptyRooms = roomService.getAllRooms().stream()
                .filter(r -> "TRONG".equals(r.getStatus())).count();
        long rentedRooms = totalRooms - emptyRooms;

        model.addAttribute("totalRooms", totalRooms);
        model.addAttribute("totalTenants", totalTenants);
        model.addAttribute("emptyRooms", emptyRooms);
        model.addAttribute("rentedRooms", rentedRooms);

        return "dashboard"; // Trỏ tới file dashboard.html làm trang chủ
    }
}