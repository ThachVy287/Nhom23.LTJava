
package com.example.demo.controller;

import com.example.demo.model.Room;
import com.example.demo.model.UtilityService;
import com.example.demo.service.RoomService;
import com.example.demo.service.ServiceManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private RoomService roomService;

    @Autowired
    private ServiceManager serviceManager;

    // Thông tin liên hệ chủ trọ lấy từ application.properties
    @Value("${app.owner.phone:}")
    private String ownerPhone;

    @Value("${app.owner.zalo-url:}")
    private String ownerZaloUrl;

    @Value("${app.owner.address:}")
    private String ownerAddress;

    // Hiển thị trang công khai /public
    @GetMapping("/public")
    public String viewHomePage(Model model) {

        model.addAttribute(
                "rooms",
                roomService.getAllRooms().stream()
                        .filter(room -> "TRONG".equals(room.getStatus()))
                        .toList()
        );

        return "index";
    }

    // Hiển thị chi tiết phòng
    @GetMapping("/rooms/detail/{id}")
    public String viewRoomDetail(
            @PathVariable Long id,
            Model model) {

        // Lấy thông tin phòng
        Room room = roomService.getRoomById(id);

        // Lấy danh sách dịch vụ từ database
        List<UtilityService> services =
                serviceManager.getAllServices();

        // Tìm dịch vụ Điện
        UtilityService electricityService =
                findServiceByName(services, "Điện");

        // Tìm dịch vụ Nước
        UtilityService waterService =
                findServiceByName(services, "Nước");

        // Chuẩn hóa thông tin liên hệ
        String phone = normalize(ownerPhone);
        String zaloUrl = normalize(ownerZaloUrl);
        String address = normalize(ownerAddress);
       

        // Tạo đường dẫn Google Maps từ địa chỉ
        String mapsUrl = "";

        if (!address.isEmpty()) {
            mapsUrl = "https://www.google.com/maps/search/?api=1&query="
                    + URLEncoder.encode(address, StandardCharsets.UTF_8);
        }

        // Truyền thông tin phòng và dịch vụ sang room-detail.html
        model.addAttribute("room", room);
        model.addAttribute("electricityService", electricityService);
        model.addAttribute("waterService", waterService);

        // Truyền thông tin liên hệ chủ trọ
        model.addAttribute("ownerPhone", phone);
        model.addAttribute("ownerZaloUrl", zaloUrl);
        model.addAttribute("ownerAddress", address);
        model.addAttribute("ownerMapsUrl", mapsUrl);

        return "room-detail";
    }

    // Tìm dịch vụ theo tên, không phân biệt chữ hoa và chữ thường
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

    // Chuẩn hóa chuỗi, tránh lỗi khi giá trị cấu hình bị null
    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
