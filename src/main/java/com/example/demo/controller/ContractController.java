package com.example.demo.controller;

import com.example.demo.model.Contract;
import com.example.demo.service.ContractService;
import com.example.demo.service.RoomService;
import com.example.demo.service.TenantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/contracts")
public class ContractController {

    @Autowired
    private ContractService contractService;

    @Autowired
    private RoomService roomService;

    @Autowired
    private TenantService tenantService;

    // Hiển thị danh sách hợp đồng
    @GetMapping
    public String viewContracts(Model model) {
        model.addAttribute("contracts", contractService.getAllContracts());
        return "contracts"; // Trỏ tới file contracts.html
    }

    // Hiển thị form tạo hợp đồng mới
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("contract", new Contract());
        model.addAttribute("rooms", roomService.getAllRooms());
        model.addAttribute("tenants", tenantService.getAllTenants());
        return "contract-form"; // Trỏ tới file contract-form.html
    }

    // Lưu hợp đồng
    @PostMapping("/save")
    public String saveContract(@ModelAttribute("contract") Contract contract) {
        contractService.saveContract(contract);
        return "redirect:/contracts";
    }

    // Xóa / Thanh lý hợp đồng
    @GetMapping("/delete/{id}")
    public String deleteContract(@PathVariable Long id) {
        contractService.deleteContract(id);
        return "redirect:/contracts";
    }
}