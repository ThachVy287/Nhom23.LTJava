package com.example.demo.controller;

import com.example.demo.model.UtilityService;
import com.example.demo.service.ServiceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/services")
public class ServiceController {

    @Autowired
    private ServiceManager serviceManager;

    // 1. Hiển thị danh sách dịch vụ
    @GetMapping
    public String viewServices(Model model) {
        model.addAttribute("services", serviceManager.getAllServices());
        return "services"; // Trỏ tới file services.html
    }

    // 2. Hiển thị form thêm mới dịch vụ
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("service", new UtilityService());
        return "service-form"; // Trỏ tới file service-form.html
    }

    // 5. MỚI: Hiển thị form cập nhật (Sửa) thông tin dịch vụ theo ID
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        UtilityService service = serviceManager.getServiceById(id); // Lấy thông tin dịch vụ hiện tại theo ID
        model.addAttribute("service", service);
        return "service-form"; // Dùng chung file service-form.html
    }

    // 3. Xử lý lưu dịch vụ (Dùng chung cho cả Thêm mới và Cập nhật)
    @PostMapping("/save")
    public String saveService(@ModelAttribute("service") UtilityService service, RedirectAttributes redirectAttributes) {
        serviceManager.saveService(service);
        redirectAttributes.addFlashAttribute("successMessage", "Lưu dịch vụ thành công!");
        return "redirect:/services";
    }

    // 4. Xóa dịch vụ có bắt lỗi ràng buộc khóa ngoại khi đã được dùng trong hóa đơn
    @GetMapping("/delete/{id}")
    public String deleteService(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            serviceManager.deleteService(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa dịch vụ thành công!");
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa dịch vụ này vì đã được sử dụng trong các Hóa đơn hoặc dữ liệu liên quan!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi khi xóa dịch vụ!");
        }
        return "redirect:/services";
    }
}