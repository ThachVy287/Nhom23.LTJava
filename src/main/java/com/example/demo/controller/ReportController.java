package com.example.demo.controller;

import com.example.demo.model.Invoice;
import com.example.demo.service.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/reports")
public class ReportController {

    @Autowired
    private InvoiceService invoiceService;

    @GetMapping
    public String viewReport(Model model) {
        List<Invoice> allInvoices = invoiceService.getAllInvoices();

        // Lọc danh sách hóa đơn đã thanh toán
        List<Invoice> paidInvoices = allInvoices.stream()
                .filter(inv -> "DA_THANH_TOAN".equals(inv.getStatus()))
                .collect(Collectors.toList());

        // Lọc danh sách hóa đơn chưa thanh toán
        List<Invoice> unpaidInvoices = allInvoices.stream()
                .filter(inv -> !"DA_THANH_TOAN".equals(inv.getStatus()))
                .collect(Collectors.toList());

        // Tính tổng doanh thu đã thu
        double totalRevenue = paidInvoices.stream()
                .mapToDouble(inv -> inv.getTotalAmount() != null ? inv.getTotalAmount() : 0)
                .sum();

        // Tính tổng tiền nợ chưa thu
        double totalUnpaid = unpaidInvoices.stream()
                .mapToDouble(inv -> inv.getTotalAmount() != null ? inv.getTotalAmount() : 0)
                .sum();

        model.addAttribute("paidInvoices", paidInvoices);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("totalUnpaid", totalUnpaid);
        model.addAttribute("paidCount", paidInvoices.size());
        model.addAttribute("unpaidCount", unpaidInvoices.size());

        return "reports"; // Trỏ tới file reports.html
    }
}