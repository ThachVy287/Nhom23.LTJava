package com.example.demo.controller;

import com.example.demo.model.Invoice;
import com.example.demo.service.InvoiceService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/reports")
public class ReportController {

    private static final String PAID = "DA_THANH_TOAN";
    private static final String UNPAID = "CHUA_THANH_TOAN";

    @Autowired
    private InvoiceService invoiceService;

    @GetMapping
    public String viewReport(
            @RequestParam(required = false) Integer year,
            Model model) {

        // Lấy toàn bộ hóa đơn
        List<Invoice> allInvoices = invoiceService.getAllInvoices();

        // Năm mặc định là năm hiện tại
        int currentYear = Year.now().getValue();

        // Chặn các giá trị năm không hợp lý
        int selectedYear = (year != null
                && year >= 2000
                && year <= currentYear + 10)
                ? year
                : currentYear;

        // Tạo danh sách năm có dữ liệu và thêm năm hiện tại
        Set<Integer> yearSet = new TreeSet<>(Comparator.reverseOrder());
        yearSet.add(currentYear);

        for (Invoice invoice : allInvoices) {
            Integer invoiceYear = getInvoiceYear(invoice);

            if (invoiceYear != null) {
                yearSet.add(invoiceYear);
            }
        }

        // Nếu người dùng chọn năm chưa có dữ liệu vẫn giữ lựa chọn đó
        yearSet.add(selectedYear);

        // Lọc hóa đơn theo năm được chọn
        List<Invoice> invoicesInYear = allInvoices.stream()
                .filter(invoice ->
                        Integer.valueOf(selectedYear).equals(
                                getInvoiceYear(invoice)
                        )
                )
                .collect(Collectors.toList());

        // Lọc hóa đơn đã thanh toán
        List<Invoice> paidInvoices = invoicesInYear.stream()
                .filter(invoice -> PAID.equals(invoice.getStatus()))
                .sorted(
                        Comparator.comparing(
                                Invoice::getMonthYear,
                                Comparator.nullsLast(Comparator.reverseOrder())
                        )
                )
                .collect(Collectors.toList());

        // Lọc hóa đơn chưa thanh toán
        List<Invoice> unpaidInvoices = invoicesInYear.stream()
                .filter(invoice -> UNPAID.equals(invoice.getStatus()))
                .collect(Collectors.toList());

        // Tính tổng doanh thu đã thu
        BigDecimal totalRevenue = sumInvoiceAmounts(paidInvoices);

        // Tính tổng tiền chưa thu
        BigDecimal totalUnpaid = sumInvoiceAmounts(unpaidInvoices);

        // Khởi tạo doanh thu 12 tháng, mặc định bằng 0
        List<BigDecimal> monthlyRevenue = new ArrayList<>();

        for (int month = 0; month < 12; month++) {
            monthlyRevenue.add(BigDecimal.ZERO);
        }

        // Cộng doanh thu đã thanh toán vào từng tháng
        for (Invoice invoice : paidInvoices) {

            YearMonth monthYear = parseMonthYear(invoice.getMonthYear());

            if (monthYear == null) {
                continue;
            }

            int monthIndex = monthYear.getMonthValue() - 1;

            BigDecimal amount = toBigDecimal(invoice.getTotalAmount());

            monthlyRevenue.set(
                    monthIndex,
                    monthlyRevenue.get(monthIndex).add(amount)
            );
        }

        // Chuyển dữ liệu sang dạng số để biểu đồ JavaScript sử dụng
        List<Double> monthlyRevenueData = monthlyRevenue.stream()
                .map(BigDecimal::doubleValue)
                .collect(Collectors.toList());

        // Truyền dữ liệu sang reports.html
        model.addAttribute("paidInvoices", paidInvoices);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("totalUnpaid", totalUnpaid);

        model.addAttribute("paidCount", paidInvoices.size());
        model.addAttribute("unpaidCount", unpaidInvoices.size());
        model.addAttribute("totalInvoices", invoicesInYear.size());

        model.addAttribute("selectedYear", selectedYear);
        model.addAttribute("years", new ArrayList<>(yearSet));

        model.addAttribute("monthlyRevenueData", monthlyRevenueData);

        return "reports";
    }

    // Tính tổng tiền từ danh sách hóa đơn
    private BigDecimal sumInvoiceAmounts(List<Invoice> invoices) {

        BigDecimal total = BigDecimal.ZERO;

        for (Invoice invoice : invoices) {
            total = total.add(toBigDecimal(invoice.getTotalAmount()));
        }

        return total;
    }

    // Chuyển số tiền Double sang BigDecimal an toàn
    private BigDecimal toBigDecimal(Double amount) {

        if (amount == null || !Double.isFinite(amount)) {
            return BigDecimal.ZERO;
        }

        return BigDecimal.valueOf(amount);
    }

    // Lấy năm từ kỳ hóa đơn yyyy-MM
    private Integer getInvoiceYear(Invoice invoice) {

        if (invoice == null || invoice.getMonthYear() == null) {
            return null;
        }

        YearMonth monthYear = parseMonthYear(invoice.getMonthYear());

        return monthYear != null ? monthYear.getYear() : null;
    }

    // Phân tích kỳ hóa đơn
    private YearMonth parseMonthYear(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return YearMonth.parse(value.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}