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

    // 2. Hiển thị form lập hóa đơn mới
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("invoice", new Invoice());
        model.addAttribute("rooms", roomService.getAllRooms());
        return "invoice-form"; // Trỏ tới file invoice-form.html
    }

    // 5. MỚI: Hiển thị form cập nhật (Sửa) hóa đơn theo ID
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Invoice invoice = invoiceService.getInvoiceById(id); // Lấy thông tin hóa đơn hiện tại
        model.addAttribute("invoice", invoice);
        model.addAttribute("rooms", roomService.getAllRooms());
        return "invoice-form"; // Dùng chung file invoice-form.html
    }

    // 3. Xử lý lưu hóa đơn (Dùng chung cho cả Thêm mới và Cập nhật)
    @PostMapping("/save")
    public String saveInvoice(@ModelAttribute("invoice") Invoice invoice, RedirectAttributes redirectAttributes) {
        invoiceService.saveInvoice(invoice);
        redirectAttributes.addFlashAttribute("successMessage", "Lưu thông tin hóa đơn thành công!");
        return "redirect:/invoices";
    }

    // 4. Xóa hóa đơn có bắt lỗi ràng buộc khóa ngoại
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