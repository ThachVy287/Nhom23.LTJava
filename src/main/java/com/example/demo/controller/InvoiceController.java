package com.example.demo.controller;

import com.example.demo.model.Invoice;
import com.example.demo.service.InvoiceService;
import com.example.demo.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/invoices")
public class InvoiceController {

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private RoomService roomService;

    @GetMapping
    public String viewInvoices(Model model) {
        model.addAttribute("invoices", invoiceService.getAllInvoices());
        return "invoices";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("invoice", new Invoice());
        model.addAttribute("rooms", roomService.getAllRooms());
        return "invoice-add";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Invoice invoice = invoiceService.getInvoiceById(id);
        model.addAttribute("invoice", invoice);
        model.addAttribute("rooms", roomService.getAllRooms());
        return "invoice-edit";
    }

    @PostMapping("/save")
    public String saveInvoice(@ModelAttribute("invoice") Invoice invoice, RedirectAttributes redirectAttributes) {
        try {
            invoiceService.saveInvoice(invoice);
            redirectAttributes.addFlashAttribute("successMessage", "Lưu thông tin hóa đơn thành công!");
        } catch (RuntimeException e) {
            // Bắt lỗi ràng buộc điện nước và hiển thị thông báo lỗi
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            if (invoice.getId() != null) {
                return "redirect:/invoices/edit/" + invoice.getId();
            }
            return "redirect:/invoices/add";
        }
        return "redirect:/invoices";
    }

    @GetMapping("/delete/{id}")
    public String deleteInvoice(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            invoiceService.deleteInvoice(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa hóa đơn thành công!");
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa hóa đơn này vì đang có dữ liệu liên quan!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi khi xóa hóa đơn!");
        }
        return "redirect:/invoices";
    }
}