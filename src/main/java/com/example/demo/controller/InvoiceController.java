
package com.example.demo.controller;

import com.example.demo.model.Invoice;
import com.example.demo.model.UtilityService;

import com.example.demo.service.InvoiceService;
import com.example.demo.service.RoomService;
import com.example.demo.service.ServiceManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/invoices")
public class InvoiceController {

    private static final String ELECTRICITY = "Điện";
    private static final String WATER = "Nước";

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private RoomService roomService;

    @Autowired
    private ServiceManager serviceManager;

    // Danh sách hóa đơn
    @GetMapping
    public String viewInvoices(Model model) {

        model.addAttribute(
                "invoices",
                invoiceService.getAllInvoices()
        );

        return "invoices";
    }

    // Hiển thị form lập hóa đơn mới
    @GetMapping("/add")
    public String showAddForm(Model model) {

        Invoice invoice = new Invoice();
        invoice.setStatus("CHUA_THANH_TOAN");

        model.addAttribute("invoice", invoice);
        model.addAttribute("rooms", roomService.getAllRooms());

        addCurrentServiceInfo(model);

        return "invoice-add";
    }

    // Hiển thị form chỉnh sửa hóa đơn
    @GetMapping("/edit/{id}")
    public String showEditForm(
            @PathVariable Long id,
            Model model) {

        Invoice invoice = invoiceService.getInvoiceById(id);

        model.addAttribute("invoice", invoice);
        model.addAttribute("rooms", roomService.getAllRooms());

        addHistoricalServiceInfo(model, invoice);

        return "invoice-edit";
    }

    // Thêm mới hoặc cập nhật hóa đơn
    @PostMapping("/save")
    public String saveInvoice(
            @ModelAttribute("invoice") Invoice invoice,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {

            invoiceService.saveInvoice(invoice);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Lưu thông tin hóa đơn thành công!"
            );

            return "redirect:/invoices";

        } catch (DataIntegrityViolationException e) {

            model.addAttribute(
                    "errorMessage",
                    "Không thể lưu hóa đơn vì dữ liệu bị trùng " +
                    "hoặc vi phạm ràng buộc database!"
            );

        } catch (RuntimeException e) {

            model.addAttribute(
                    "errorMessage",
                    e.getMessage() != null
                            ? e.getMessage()
                            : "Không thể lưu thông tin hóa đơn!"
            );
        }

        // Nạp lại danh sách phòng
        model.addAttribute(
                "rooms",
                roomService.getAllRooms()
        );

        // Trả lại đúng form và giữ thông tin đã nhập
        if (invoice.getId() == null) {

            addCurrentServiceInfo(model);

            return "invoice-add";

        } else {

            try {

                Invoice existingInvoice =
                        invoiceService.getInvoiceById(invoice.getId());

                // Khôi phục đơn giá lịch sử từ database
                invoice.setElectricityPrice(
                        existingInvoice.getElectricityPrice()
                );

                invoice.setWaterPrice(
                        existingInvoice.getWaterPrice()
                );

                addHistoricalServiceInfo(model, existingInvoice);

            } catch (RuntimeException ignored) {
                // Không ghi đè lỗi lưu hóa đơn bằng lỗi phụ.
            }

            return "invoice-edit";
        }
    }

    // Xóa hóa đơn bằng POST
    @PostMapping("/delete/{id}")
    public String deleteInvoice(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {

            invoiceService.deleteInvoice(id);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Xóa hóa đơn thành công!"
            );

        } catch (DataIntegrityViolationException e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Không thể xóa hóa đơn này vì đang có dữ liệu liên quan!"
            );

        } catch (RuntimeException e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage() != null
                            ? e.getMessage()
                            : "Không thể xóa hóa đơn!"
            );

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Đã xảy ra lỗi khi xóa hóa đơn!"
            );
        }

        return "redirect:/invoices";
    }

    // =====================================================
    // CÁC PHƯƠNG THỨC HỖ TRỢ
    // =====================================================

    // Tìm dịch vụ theo tên, không phân biệt chữ hoa/chữ thường
    private UtilityService findServiceByName(
            List<UtilityService> services,
            String serviceName) {

        if (services == null || serviceName == null) {
            return null;
        }

        return services.stream()
                .filter(service ->
                        service.getServiceName() != null &&
                        service.getServiceName().trim()
                                .equalsIgnoreCase(serviceName)
                )
                .findFirst()
                .orElse(null);
    }

    // Truyền đơn giá dịch vụ hiện tại cho form lập hóa đơn mới
    private void addCurrentServiceInfo(Model model) {

        List<UtilityService> services =
                serviceManager.getAllServices();

        UtilityService electricityService =
                findServiceByName(services, ELECTRICITY);

        UtilityService waterService =
                findServiceByName(services, WATER);

        model.addAttribute(
                "electricityPrice",
                electricityService != null
                        ? electricityService.getPrice()
                        : null
        );

        model.addAttribute(
                "electricityUnit",
                electricityService != null
                        ? electricityService.getUnit()
                        : null
        );

        model.addAttribute(
                "waterPrice",
                waterService != null
                        ? waterService.getPrice()
                        : null
        );

        model.addAttribute(
                "waterUnit",
                waterService != null
                        ? waterService.getUnit()
                        : null
        );

        if (electricityService == null || waterService == null ||
                electricityService.getPrice() == null ||
                waterService.getPrice() == null) {

            model.addAttribute(
                    "serviceConfigWarning",
                    "Chưa cấu hình đầy đủ đơn giá dịch vụ Điện và Nước. " +
                    "Vui lòng kiểm tra Quản lý dịch vụ trước khi lập hóa đơn."
            );
        }
    }

    // Truyền đơn giá lịch sử cho form chỉnh sửa
    private void addHistoricalServiceInfo(
            Model model,
            Invoice invoice) {

        List<UtilityService> services =
                serviceManager.getAllServices();

        UtilityService electricityService =
                findServiceByName(services, ELECTRICITY);

        UtilityService waterService =
                findServiceByName(services, WATER);

        // Giá hiển thị lấy từ hóa đơn đã lưu
        model.addAttribute(
                "electricityPrice",
                invoice.getElectricityPrice()
        );

        model.addAttribute(
                "waterPrice",
                invoice.getWaterPrice()
        );

        // Đơn vị lấy từ cấu hình dịch vụ
        model.addAttribute(
                "electricityUnit",
                electricityService != null
                        ? electricityService.getUnit()
                        : null
        );

        model.addAttribute(
                "waterUnit",
                waterService != null
                        ? waterService.getUnit()
                        : null
        );
    }
}
