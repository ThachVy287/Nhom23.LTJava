
package com.example.demo.controller;

import com.example.demo.model.Room;
import com.example.demo.service.RoomService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Controller
public class PublicController {

    @Autowired
    private RoomService roomService;

    // Lấy địa chỉ khu trọ từ application.properties
    @Value("${app.owner.address:}")
    private String ownerAddress;

    // Trang chủ dành cho khách hàng
    @GetMapping("/")
    public String viewPublicRooms(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String minPrice,
            @RequestParam(required = false) String maxPrice,
            @RequestParam(required = false) String minArea,
            @RequestParam(required = false) String maxArea,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size,
            Model model) {

        // Kiểm tra tham số phân trang
        int safePage = Math.max(page, 0);
        int safeSize = (size > 0 && size <= 50) ? size : 6;

        // Chuẩn hóa từ khóa tìm kiếm
        String searchKeyword = normalize(keyword);

        // Chỉ chấp nhận 3 kiểu sắp xếp hợp lệ
        String safeSort = normalize(sort);

        if (!safeSort.equals("priceAsc")
                && !safeSort.equals("priceDesc")
                && !safeSort.equals("latest")) {

            safeSort = "latest";
        }

        // Danh sách lỗi nhập liệu
        List<String> filterErrors = new ArrayList<>();

        // Chuyển các giá trị bộ lọc từ String sang Double
        Double minPriceValue = parseFilterValue(
                minPrice, "Giá thuê tối thiểu", filterErrors);

        Double maxPriceValue = parseFilterValue(
                maxPrice, "Giá thuê tối đa", filterErrors);

        Double minAreaValue = parseFilterValue(
                minArea, "Diện tích tối thiểu", filterErrors);

        Double maxAreaValue = parseFilterValue(
                maxArea, "Diện tích tối đa", filterErrors);

        // Kiểm tra khoảng giá thuê
        if (minPriceValue != null && maxPriceValue != null
                && minPriceValue > maxPriceValue) {

            filterErrors.add(
                    "Giá thuê tối thiểu không được lớn hơn giá thuê tối đa!");

            minPriceValue = null;
            maxPriceValue = null;
        }

        // Kiểm tra khoảng diện tích
        if (minAreaValue != null && maxAreaValue != null
                && minAreaValue > maxAreaValue) {

            filterErrors.add(
                    "Diện tích tối thiểu không được lớn hơn diện tích tối đa!");

            minAreaValue = null;
            maxAreaValue = null;
        }

        // Tạo thứ tự sắp xếp
        Sort sorting = buildSort(safeSort);

        // Tạo phân trang
        Pageable pageable = PageRequest.of(
                safePage,
                safeSize,
                sorting
        );

        // Lấy phòng theo các điều kiện tìm kiếm và sắp xếp
        Page<Room> roomPage =
                roomService.getAvailableRoomsForPublic(
                        searchKeyword,
                        minPriceValue,
                        maxPriceValue,
                        minAreaValue,
                        maxAreaValue,
                        pageable
                );

        // Nếu trang được yêu cầu vượt quá số trang hiện có,
        // chuyển về trang cuối cùng
        if (roomPage.getTotalPages() > 0
                && safePage >= roomPage.getTotalPages()) {

            safePage = roomPage.getTotalPages() - 1;

            pageable = PageRequest.of(
                    safePage,
                    safeSize,
                    sorting
            );

            roomPage = roomService.getAvailableRoomsForPublic(
                    searchKeyword,
                    minPriceValue,
                    maxPriceValue,
                    minAreaValue,
                    maxAreaValue,
                    pageable
            );
        }

        // Chuẩn hóa địa chỉ khu trọ
        String address = normalize(ownerAddress);

        // Tạo đường dẫn Google Maps
        String mapsUrl = "";
        String mapEmbedUrl = "";

        if (!address.isEmpty()) {

            String encodedAddress = URLEncoder.encode(
                    address,
                    StandardCharsets.UTF_8
            );

            mapsUrl = "https://www.google.com/maps/search/?api=1&query="
                    + encodedAddress;

            mapEmbedUrl = "https://www.google.com/maps?q="
                    + encodedAddress
                    + "&output=embed";
        }

        // Truyền dữ liệu phòng sang giao diện
        model.addAttribute("rooms", roomPage.getContent());
        model.addAttribute("roomPage", roomPage);
        model.addAttribute("keyword", searchKeyword);

        // Giữ lại các giá trị bộ lọc trên form
        model.addAttribute("minPrice", normalize(minPrice));
        model.addAttribute("maxPrice", normalize(maxPrice));
        model.addAttribute("minArea", normalize(minArea));
        model.addAttribute("maxArea", normalize(maxArea));

        // Truyền kiểu sắp xếp đang sử dụng
        model.addAttribute("sort", safeSort);

        // Truyền thông báo lỗi bộ lọc
        model.addAttribute("filterErrors", filterErrors);

        // Xác định người dùng có đang sử dụng bộ lọc hay không
        boolean hasActiveFilters =
                !searchKeyword.isEmpty()
                || !normalize(minPrice).isEmpty()
                || !normalize(maxPrice).isEmpty()
                || !normalize(minArea).isEmpty()
                || !normalize(maxArea).isEmpty()
                || !safeSort.equals("latest");

        model.addAttribute("hasActiveFilters", hasActiveFilters);

        // Truyền thông tin vị trí khu trọ
        model.addAttribute("ownerAddress", address);
        model.addAttribute("ownerMapsUrl", mapsUrl);
        model.addAttribute("ownerMapEmbedUrl", mapEmbedUrl);

        return "public-rooms";
    }

    // Xây dựng thứ tự sắp xếp
    private Sort buildSort(String sort) {

        switch (sort) {

            case "priceAsc":
                // Giá thấp đến cao, nếu bằng giá thì phòng mới hơn trước
                return Sort.by(Sort.Direction.ASC, "price")
                        .and(Sort.by(Sort.Direction.DESC, "id"));

            case "priceDesc":
                // Giá cao đến thấp, nếu bằng giá thì phòng mới hơn trước
                return Sort.by(Sort.Direction.DESC, "price")
                        .and(Sort.by(Sort.Direction.DESC, "id"));

            case "latest":
            default:
                // Phòng mới nhất lên trước
                return Sort.by(Sort.Direction.DESC, "id");
        }
    }

    // Chuyển giá trị bộ lọc sang Double và kiểm tra hợp lệ
    private Double parseFilterValue(
            String value,
            String fieldName,
            List<String> errors) {

        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {
            double number = Double.parseDouble(value.trim());

            if (!Double.isFinite(number) || number < 0) {
                errors.add(
                        fieldName + " phải là số hợp lệ và không được âm!");
                return null;
            }

            return number;

        } catch (NumberFormatException e) {

            errors.add(
                    fieldName + " không đúng định dạng số!");

            return null;
        }
    }

    // Chuẩn hóa chuỗi
    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
