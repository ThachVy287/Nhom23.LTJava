
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

        model.addAttribute(
                "services",
                serviceManager.getAllServices()
        );

        return "services";
    }

    // 2. Hiển thị form thêm mới dịch vụ
    @GetMapping("/add")
    public String showAddForm(Model model) {

        model.addAttribute("service", new UtilityService());

        return "service-add";
    }

    // 3. Hiển thị form cập nhật dịch vụ
    @GetMapping("/edit/{id}")
    public String showEditForm(
            @PathVariable Long id,
            Model model) {

        UtilityService service = serviceManager.getServiceById(id);

        model.addAttribute("service", service);

        return "service-edit";
    }

    // 4. Lưu dịch vụ: thêm mới hoặc cập nhật
    @PostMapping("/save")
    public String saveService(
            @ModelAttribute("service") UtilityService service,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {

            serviceManager.saveService(service);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Lưu thông tin dịch vụ thành công!"
            );

            // Chỉ chuyển hướng khi lưu thành công
            return "redirect:/services";

        } catch (DataIntegrityViolationException e) {

            model.addAttribute(
                    "errorMessage",
                    "Không thể lưu dịch vụ vì tên dịch vụ đã tồn tại " +
                    "hoặc dữ liệu đang được sử dụng ở nơi khác. " +
                    "Vui lòng kiểm tra lại!"
            );

        } catch (RuntimeException e) {

            // Giữ nguyên thông báo nghiệp vụ từ ServiceManager
            String errorMessage = e.getMessage();

            if (errorMessage == null || errorMessage.isBlank()) {
                errorMessage = "Thông tin dịch vụ không hợp lệ!";
            }

            model.addAttribute("errorMessage", errorMessage);

        } catch (Exception e) {

            model.addAttribute(
                    "errorMessage",
                    "Đã xảy ra lỗi hệ thống khi lưu dịch vụ. " +
                    "Dữ liệu bạn vừa nhập vẫn được giữ lại. Vui lòng thử lại!"
            );
        }

        // Trả về đúng form, không redirect
        // Đối tượng service vẫn giữ dữ liệu người dùng vừa nhập
        if (service.getId() == null) {

            return "service-add";

        }

        return "service-edit";
    }

    // 5. Xóa dịch vụ
    @GetMapping("/delete/{id}")
    public String deleteService(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {

            serviceManager.deleteService(id);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Xóa dịch vụ thành công!"
            );

        } catch (DataIntegrityViolationException e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Không thể xóa dịch vụ vì dữ liệu này đang được " +
                    "liên kết với thông tin khác. " +
                    "Vui lòng kiểm tra các dữ liệu liên quan trước khi xóa."
            );

        } catch (RuntimeException e) {

            // Hiển thị nguyên nhân cụ thể từ ServiceManager
            String errorMessage = e.getMessage();

            if (errorMessage == null || errorMessage.isBlank()) {
                errorMessage = "Không thể xóa dịch vụ do dữ liệu không hợp lệ!";
            }

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    errorMessage
            );

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Đã xảy ra lỗi hệ thống khi xóa dịch vụ. " +
                    "Vui lòng thử lại sau!"
            );
        }

        return "redirect:/services";
    }
}
