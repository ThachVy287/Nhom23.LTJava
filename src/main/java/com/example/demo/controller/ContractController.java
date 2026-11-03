package com.example.demo.controller;

import com.example.demo.model.Contract;
import com.example.demo.service.ContractService;
import com.example.demo.service.RoomService;
import com.example.demo.service.TenantService;

import org.springframework.beans.factory.annotation.Autowired;
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

    // Hiển thị danh sách hợp đồng
    @GetMapping
    public String viewContracts(Model model) {
        model.addAttribute("contracts", contractService.getAllContracts());
        return "contracts";
    }

    // Hiển thị form thêm hợp đồng
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("contract", new Contract());
        model.addAttribute("rooms", roomService.getAllRooms());
        model.addAttribute("tenants", tenantService.getAllTenants());

        return "contract-add";
    }

    // Hiển thị form sửa hợp đồng
    @GetMapping("/edit/{id}")
    public String showEditForm(
            @PathVariable Long id,
            Model model
    ) {
        Contract contract = contractService.getContractById(id);

        model.addAttribute("contract", contract);
        model.addAttribute("rooms", roomService.getAllRooms());
        model.addAttribute("tenants", tenantService.getAllTenants());

        return "contract-edit";
    }

    // Lưu hợp đồng
    @PostMapping("/save")
    public String saveContract(
            @ModelAttribute("contract") Contract contract,
            RedirectAttributes redirectAttributes
    ) {
        try {
            contractService.saveContract(contract);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Lưu thông tin hợp đồng thành công!"
            );

            return "redirect:/contracts";

        } catch (RuntimeException e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );

            if (contract.getId() != null) {
                return "redirect:/contracts/edit/" + contract.getId();
            }

            return "redirect:/contracts/add";
        }
    }

    // Thanh lý hợp đồng
    @PostMapping("/terminate/{id}")
    public String terminateContract(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes
    ) {
        try {
            contractService.terminateContract(id);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Thanh lý hợp đồng thành công!"
            );

        } catch (RuntimeException e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );
        }

        return "redirect:/contracts";
    }
}