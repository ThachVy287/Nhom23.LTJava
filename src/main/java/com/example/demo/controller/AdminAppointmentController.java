
package com.example.demo.controller;

import com.example.demo.model.Appointment;
import com.example.demo.service.AppointmentService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/appointments")
public class AdminAppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    private static final List<String> APPOINTMENT_STATUSES = List.of(
            "CHO_XAC_NHAN",
            "DA_XAC_NHAN",
            "DA_HOAN_THANH",
            "DA_HUY"
    );

    // Danh sách lịch đăng ký xem phòng
    @GetMapping
    public String getAppointments(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        int safePage = Math.max(page, 0);

        int safeSize = List.of(5, 10, 20, 50).contains(size)
                ? size
                : 10;

        Pageable pageable = PageRequest.of(
                safePage,
                safeSize,
                Sort.by(
                        Sort.Direction.DESC,
                        "createdAt"
                )
        );

        Page<Appointment> appointmentPage =
                appointmentService.getAppointments(
                        keyword,
                        status,
                        pageable
                );

        // Nếu trang yêu cầu vượt quá số trang hiện có
        if (appointmentPage.getTotalPages() > 0
                && safePage >= appointmentPage.getTotalPages()) {

            safePage = appointmentPage.getTotalPages() - 1;

            pageable = PageRequest.of(
                    safePage,
                    safeSize,
                    Sort.by(
                            Sort.Direction.DESC,
                            "createdAt"
                    )
            );

            appointmentPage = appointmentService.getAppointments(
                    keyword,
                    status,
                    pageable
            );
        }

        model.addAttribute("appointmentPage", appointmentPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("pageSize", safeSize);
        model.addAttribute("appointmentStatuses", APPOINTMENT_STATUSES);

        return "admin/appointments";
    }

    // Cập nhật trạng thái lịch đăng ký
    @PostMapping("/{id}/status")
    public String updateStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String filterStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            RedirectAttributes redirectAttributes) {

        try {

            appointmentService.updateAppointmentStatus(id, status);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Cập nhật trạng thái lịch đăng ký thành công!"
            );

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );
        }

        return "redirect:/admin/appointments"
                + "?keyword=" + encode(keyword)
                + "&status=" + encode(filterStatus)
                + "&page=" + Math.max(page, 0)
                + "&size=" + size;
    }

    // Chuẩn hóa giá trị đưa lên URL
    private String encode(String value) {

        if (value == null || value.isBlank()) {
            return "";
        }

        return java.net.URLEncoder.encode(
                value,
                java.nio.charset.StandardCharsets.UTF_8
        );
    }
}
