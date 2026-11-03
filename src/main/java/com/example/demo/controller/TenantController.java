
package com.example.demo.controller;

import com.example.demo.model.Tenant;
import com.example.demo.service.RoomService;
import com.example.demo.service.TenantService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/tenants")
public class TenantController {

    @Autowired
    private TenantService tenantService;

    @Autowired
    private RoomService roomService;

    // 1. Hiển thị danh sách khách thuê
    @GetMapping
    public String viewTenants(Model model) {

        model.addAttribute("tenants", tenantService.getAllTenants());

        return "tenants";
    }

    // 2. Hiển thị form thêm khách thuê
    @GetMapping("/add")
    public String showAddForm(Model model) {

        model.addAttribute("tenant", new Tenant());
        model.addAttribute("rooms", roomService.getAllRooms());

        return "tenant-add";
    }

    // 3. Hiển thị form cập nhật khách thuê
    @GetMapping("/edit/{id}")
    public String showEditForm(
            @PathVariable Long id,
            Model model) {

        Tenant tenant = tenantService.getTenantById(id);

        model.addAttribute("tenant", tenant);
        model.addAttribute("rooms", roomService.getAllRooms());

        return "tenant-edit";
    }

    // 4. Lưu khách thuê: thêm mới hoặc cập nhật
    @PostMapping("/save")
    public String saveTenant(
            @ModelAttribute("tenant") Tenant tenant,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {

            tenantService.saveTenant(tenant);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Lưu thông tin khách thuê thành công!"
            );

            // Chỉ chuyển hướng khi lưu thành công
            return "redirect:/tenants";

        } catch (DataIntegrityViolationException e) {

            model.addAttribute(
                    "errorMessage",
                    "Không thể lưu thông tin khách thuê vì số điện thoại " +
                    "hoặc CCCD đã tồn tại, hoặc dữ liệu đang được sử dụng " +
                    "ở nơi khác. Vui lòng kiểm tra lại!"
            );

        } catch (RuntimeException e) {

            // Giữ nguyên thông báo cụ thể từ TenantService
            String errorMessage = e.getMessage();

            if (errorMessage == null || errorMessage.isBlank()) {
                errorMessage = "Thông tin khách thuê không hợp lệ!";
            }

            model.addAttribute("errorMessage", errorMessage);

        } catch (Exception e) {

            model.addAttribute(
                    "errorMessage",
                    "Đã xảy ra lỗi hệ thống khi lưu khách thuê. " +
                    "Dữ liệu bạn vừa nhập vẫn được giữ lại. Vui lòng thử lại!"
            );
        }

        // QUAN TRỌNG:
        // Nạp lại danh sách phòng để dropdown vẫn hoạt động
        model.addAttribute("rooms", roomService.getAllRooms());

        // Trả về đúng form ban đầu, không redirect
        // Đối tượng tenant vẫn giữ dữ liệu người dùng vừa nhập
        if (tenant.getId() == null) {
            return "tenant-add";
        }

        return "tenant-edit";
    }

    // 5. Xóa khách thuê
    @GetMapping("/delete/{id}")
    public String deleteTenant(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {

            tenantService.deleteTenant(id);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Xóa khách thuê thành công!"
            );

        } catch (DataIntegrityViolationException e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Không thể xóa khách thuê vì hồ sơ này đang được " +
                    "liên kết với hợp đồng hoặc dữ liệu liên quan. " +
                    "Vui lòng kiểm tra thông tin liên kết trước khi xóa."
            );

        } catch (RuntimeException e) {

            // Hiển thị nguyên nhân cụ thể từ Service
            String errorMessage = e.getMessage();

            if (errorMessage == null || errorMessage.isBlank()) {
                errorMessage = "Không thể xóa khách thuê do dữ liệu không hợp lệ!";
            }

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    errorMessage
            );

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Đã xảy ra lỗi hệ thống khi xóa khách thuê. " +
                    "Vui lòng thử lại sau!"
            );
        }

        return "redirect:/tenants";
    }
}
