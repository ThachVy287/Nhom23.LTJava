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

    // 1. Hiển thị danh sách hóa đơn
    @GetMapping
    public String viewInvoices(Model model) {
        model.addAttribute("invoices", invoiceService.getAllInvoices());
        return "invoices"; // Trỏ tới file invoices.html
    }

    // 2. Hiển thị form THÊM MỚI hóa đơn
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("invoice", new Invoice());
        model.addAttribute("rooms", roomService.getAllRooms());
        return "invoice-add"; // Trỏ tới file invoice-add.html
    }

    // 3. Hiển thị form CẬP NHẬT hóa đơn theo ID
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Invoice invoice = invoiceService.getInvoiceById(id);
        model.addAttribute("invoice", invoice);
        model.addAttribute("rooms", roomService.getAllRooms());
        return "invoice-edit"; // Trỏ tới file invoice-edit.html
    }

    // 4. Xử lý lưu hóa đơn chung (cho cả thêm mới và cập nhật)
    @PostMapping("/save")
    public String saveInvoice(@ModelAttribute("invoice") Invoice invoice, RedirectAttributes redirectAttributes) {
        invoiceService.saveInvoice(invoice);
        redirectAttributes.addFlashAttribute("successMessage", "Lưu thông tin hóa đơn thành công!");
        return "redirect:/invoices";
    }

    // 5. Xóa hóa đơn có bắt lỗi ràng buộc khóa ngoại
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