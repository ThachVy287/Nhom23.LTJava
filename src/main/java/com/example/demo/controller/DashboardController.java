package com.example.demo.controller;

import com.example.demo.model.Invoice;
import com.example.demo.service.RoomService;
import com.example.demo.service.TenantService;
import com.example.demo.service.ContractService;
import com.example.demo.service.InvoiceService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.util.List;

@Controller
public class DashboardController {

    private static final String ROOM_AVAILABLE = "TRONG";
    private static final String ROOM_RENTED = "DA_THUE";
    private static final String INVOICE_PAID = "DA_THANH_TOAN";

    @Autowired
    private RoomService roomService;

    @Autowired
    private TenantService tenantService;

    @Autowired
    private ContractService contractService;

    @Autowired
    private InvoiceService invoiceService;

    // Trang tổng quan quản trị
    @GetMapping("/admin")
    public String dashboard(Model model) {

        // 1. Lấy dữ liệu từ các Service
        var rooms = roomService.getAllRooms();
        var tenants = tenantService.getAllTenants();
        var contracts = contractService.getAllContracts();
        List<Invoice> invoices = invoiceService.getAllInvoices();

        // 2. Thống kê phòng
        long totalRooms = rooms.size();

        long emptyRooms = rooms.stream()
                .filter(room -> ROOM_AVAILABLE.equals(room.getStatus()))
                .count();

        long rentedRooms = rooms.stream()
                .filter(room -> ROOM_RENTED.equals(room.getStatus()))
                .count();

        // 3. Thống kê khách thuê
        long totalTenants = tenants.size();

        // 4. Thống kê hợp đồng
        long totalContracts = contracts.size();

        // 5. Thống kê hóa đơn
        long totalInvoices = invoices.size();

        // 6. Tính doanh thu từ hóa đơn đã thanh toán
        BigDecimal totalRevenue = invoices.stream()
                .filter(invoice ->
                        INVOICE_PAID.equals(invoice.getStatus())
                )
                .map(Invoice::getTotalAmount)
                .filter(amount ->
                        amount != null && Double.isFinite(amount)
                )
                .map(BigDecimal::valueOf)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 7. Truyền dữ liệu sang dashboard.html
        model.addAttribute("totalRooms", totalRooms);
        model.addAttribute("emptyRooms", emptyRooms);
        model.addAttribute("rentedRooms", rentedRooms);
        model.addAttribute("totalTenants", totalTenants);
        model.addAttribute("totalContracts", totalContracts);
        model.addAttribute("totalInvoices", totalInvoices);
        model.addAttribute("totalRevenue", totalRevenue);

        return "dashboard";
    }
}