package com.example.demo.controller;

import com.example.demo.model.Contract;
import com.example.demo.service.ContractService;
import com.example.demo.service.RoomService;
import com.example.demo.service.TenantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/contracts")
public class ContractController {

    @Autowired
    private ContractService contractService;

    @Autowired
    private RoomService roomService;

    @Autowired
    private TenantService tenantService;

    // 1. Hiển thị danh sách hợp đồng
    @GetMapping
    public String viewContracts(Model model) {
        model.addAttribute("contracts", contractService.getAllContracts());
        return "contracts"; // Trỏ tới file contracts.html
    }

    // 2. Hiển thị form THÊM MỚI hợp đồng
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("contract", new Contract());
        model.addAttribute("rooms", roomService.getAllRooms());
        model.addAttribute("tenants", tenantService.getAllTenants());
        return "contract-add"; // Trỏ tới file contract-add.html
    }

    // 3. Hiển thị form CẬP NHẬT hợp đồng theo ID
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Contract contract = contractService.getContractById(id);
        model.addAttribute("contract", contract);
        model.addAttribute("rooms", roomService.getAllRooms());
        model.addAttribute("tenants", tenantService.getAllTenants());
        return "contract-edit"; // Trỏ tới file contract-edit.html
    }

    // 4. Xử lý lưu hợp đồng chung (cho cả thêm mới và cập nhật)
    @PostMapping("/save")
    public String saveContract(@ModelAttribute("contract") Contract contract, RedirectAttributes redirectAttributes) {
        contractService.saveContract(contract);
        redirectAttributes.addFlashAttribute("successMessage", "Lưu thông tin hợp đồng thành công!");
        return "redirect:/contracts";
    }

    // 5. Xóa / Thanh lý hợp đồng có bắt lỗi khóa ngoại
    @GetMapping("/delete/{id}")
    public String deleteContract(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            contractService.deleteContract(id);
            redirectAttributes.addFlashAttribute("successMessage", "Thanh lý / Xóa hợp đồng thành công!");
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa hợp đồng này vì đang có Hóa đơn hoặc dữ liệu liên quan!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi khi thanh lý hợp đồng!");
        }
        return "redirect:/contracts";
    }
}