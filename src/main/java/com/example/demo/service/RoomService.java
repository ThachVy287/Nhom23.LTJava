
package com.example.demo.service;

import com.example.demo.model.Room;
import com.example.demo.repository.ContractRepository;
import com.example.demo.repository.InvoiceRepository;
import com.example.demo.repository.RoomRepository;
import com.example.demo.repository.TenantRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class RoomService {

    private static final String ROOM_AVAILABLE = "TRONG";
    private static final String ROOM_RENTED = "DA_THUE";

    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024;
    private static final int MAX_IMAGE_COUNT = 10;

    private static final String IMAGE_UPLOAD_DIR =
            "src/main/resources/static/images/";

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    // Lấy toàn bộ phòng
    @Transactional(readOnly = true)
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    // Lấy danh sách phòng có tìm kiếm, lọc và phân trang
    @Transactional(readOnly = true)
    public Page<Room> getRooms(
            String keyword,
            String status,
            Pageable pageable) {

        String searchKeyword = keyword == null ? "" : keyword.trim();
        String searchStatus = status == null ? "" : status.trim();

        boolean hasKeyword = !searchKeyword.isEmpty();
        boolean hasStatus = !searchStatus.isEmpty();

        if (hasKeyword && hasStatus) {
            return roomRepository
                    .findByRoomNumberContainingIgnoreCaseAndStatus(
                            searchKeyword, searchStatus, pageable);
        }

        if (hasKeyword) {
            return roomRepository.findByRoomNumberContainingIgnoreCase(
                    searchKeyword, pageable);
        }

        if (hasStatus) {
            return roomRepository.findByStatus(searchStatus, pageable);
        }

        return roomRepository.findAll(pageable);
    }

    // Tìm phòng theo ID
    @Transactional(readOnly = true)
    public Room getRoomById(Long id) {

        if (id == null) {
            throw new RuntimeException("ID phòng không hợp lệ!");
        }

        return roomRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy phòng trọ có ID: " + id
                        ));
    }

    // Thêm mới hoặc cập nhật phòng và hình ảnh
    @Transactional
    public void saveRoomWithImages(
            Room formRoom,
            MultipartFile[] imageFiles) throws IOException {

        if (formRoom == null) {
            throw new RuntimeException(
                    "Thông tin phòng không hợp lệ!");
        }

        // 1. Kiểm tra số phòng
        if (formRoom.getRoomNumber() == null ||
                formRoom.getRoomNumber().trim().isEmpty()) {

            throw new RuntimeException("Vui lòng nhập số phòng!");
        }

        String roomNumber = formRoom.getRoomNumber().trim();
        Long roomId = formRoom.getId();

        boolean duplicated;

        if (roomId == null) {
            duplicated =
                    roomRepository.existsByRoomNumberIgnoreCase(roomNumber);
        } else {
            duplicated =
                    roomRepository.existsByRoomNumberIgnoreCaseAndIdNot(
                            roomNumber, roomId);
        }

        if (duplicated) {
            throw new RuntimeException(
                    "Số phòng '" + roomNumber +
                    "' đã tồn tại trong hệ thống!");
        }

        // 2. Kiểm tra giá thuê
        if (formRoom.getPrice() == null ||
                !Double.isFinite(formRoom.getPrice()) ||
                formRoom.getPrice() <= 0) {

            throw new RuntimeException(
                    "Giá thuê phòng phải là số hợp lệ và lớn hơn 0!");
        }

        // 3. Kiểm tra diện tích phòng
        if (formRoom.getRoomArea() != null &&
                (!Double.isFinite(formRoom.getRoomArea()) ||
                        formRoom.getRoomArea() <= 0)) {

            throw new RuntimeException(
                    "Diện tích phòng phải lớn hơn 0!");
        }

        // 4. Kiểm tra diện tích phòng tắm
        if (formRoom.getBathroomArea() != null &&
                (!Double.isFinite(formRoom.getBathroomArea()) ||
                        formRoom.getBathroomArea() < 0)) {

            throw new RuntimeException(
                    "Diện tích phòng tắm không được âm!");
        }

        if (formRoom.getRoomArea() != null &&
                formRoom.getBathroomArea() != null &&
                formRoom.getBathroomArea() > formRoom.getRoomArea()) {

            throw new RuntimeException(
                    "Diện tích phòng tắm không được lớn hơn diện tích phòng!");
        }

        // 5. Kiểm tra số tầng
        if (formRoom.getFloorNumber() != null &&
                formRoom.getFloorNumber() < 0) {

            throw new RuntimeException(
                    "Số tầng không được là số âm!");
        }

        // 6. Lấy phòng thực tế từ database
        Room room;

        if (roomId == null) {
            room = new Room();

            // Phòng mới mặc định là phòng trống
            room.setStatus(ROOM_AVAILABLE);

        } else {
            room = getRoomById(roomId);

            if (!ROOM_AVAILABLE.equals(room.getStatus()) &&
                    !ROOM_RENTED.equals(room.getStatus())) {

                throw new RuntimeException(
                        "Trạng thái phòng hiện tại không hợp lệ. " +
                        "Vui lòng kiểm tra lại dữ liệu!");
            }
        }

        // 7. Gán thông tin phòng
        // Không lấy trạng thái từ form để tránh thay đổi trạng thái
        // phòng trái với trạng thái hợp đồng.
        room.setRoomNumber(roomNumber);
        room.setPrice(formRoom.getPrice());
        room.setDescription(trimToNull(formRoom.getDescription()));

        room.setRoomArea(formRoom.getRoomArea());
        room.setBathroomArea(formRoom.getBathroomArea());
        room.setHasAc(formRoom.getHasAc());
        room.setFloorNumber(formRoom.getFloorNumber());
        room.setBathroomDesc(trimToNull(formRoom.getBathroomDesc()));

        // 8. Giữ lại ảnh cũ nếu form không gửi imageUrls
        String submittedImages = formRoom.getImageUrls();
        String currentImages = room.getImageUrls();

        if (submittedImages == null && currentImages != null) {
            submittedImages = currentImages;
        }

        List<String> imagePaths = new ArrayList<>();

        if (submittedImages != null && !submittedImages.isBlank()) {

            for (String image : submittedImages.split(",")) {

                String path = image.trim();

                if (!path.isEmpty() && !imagePaths.contains(path)) {
                    imagePaths.add(path);
                }
            }
        }

        // 9. Kiểm tra số lượng ảnh
        int selectedImageCount = 0;

        if (imageFiles != null) {
            for (MultipartFile file : imageFiles) {

                if (file != null && !file.isEmpty()) {
                    selectedImageCount++;
                }
            }
        }

        if (imagePaths.size() + selectedImageCount > MAX_IMAGE_COUNT) {
            throw new RuntimeException(
                    "Mỗi phòng chỉ được lưu tối đa " +
                    MAX_IMAGE_COUNT + " hình ảnh!");
        }

        // 10. Tạo thư mục lưu ảnh
        Path uploadDirectory = Paths.get(IMAGE_UPLOAD_DIR)
                .toAbsolutePath()
                .normalize();

        // Lưu lại các file mới để có thể dọn nếu lưu phòng thất bại
        List<Path> newlyUploadedFiles = new ArrayList<>();

        try {

            Files.createDirectories(uploadDirectory);

            // 11. Xử lý upload ảnh
            if (imageFiles != null) {

                for (MultipartFile file : imageFiles) {

                    if (file == null || file.isEmpty()) {
                        continue;
                    }

                    validateImage(file);

                    String extension = getImageExtension(
                            file.getOriginalFilename());

                    // Tạo tên ảnh ngẫu nhiên để tránh trùng tên
                    String fileName = UUID.randomUUID() + extension;

                    Path destination = uploadDirectory
                            .resolve(fileName)
                            .normalize();

                    if (!destination.startsWith(uploadDirectory)) {
                        throw new RuntimeException(
                                "Đường dẫn lưu hình ảnh không hợp lệ!");
                    }

                    try (InputStream inputStream = file.getInputStream()) {
                        Files.copy(inputStream, destination);
                    }

                    newlyUploadedFiles.add(destination);

                    imagePaths.add("/images/" + fileName);
                }
            }

            // 12. Gộp các đường dẫn ảnh
            room.setImageUrls(String.join(",", imagePaths));

            // 13. Lưu phòng và thực hiện flush để phát hiện
            // lỗi ràng buộc database ngay tại đây.
            roomRepository.saveAndFlush(room);

        } catch (IOException e) {

            cleanupUploadedFiles(newlyUploadedFiles);

            throw e;

        } catch (RuntimeException e) {

            cleanupUploadedFiles(newlyUploadedFiles);

            throw e;
        }
    }

    // Xóa các ảnh vừa tải lên khi lưu phòng thất bại
    private void cleanupUploadedFiles(List<Path> uploadedFiles) {

        for (Path uploadedFile : uploadedFiles) {

            try {
                Files.deleteIfExists(uploadedFile);

            } catch (IOException ignored) {
                // Không ghi đè lỗi ban đầu bằng lỗi dọn file.
            }
        }
    }

    // Kiểm tra định dạng và dung lượng hình ảnh
    private void validateImage(MultipartFile file) {

        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new RuntimeException(
                    "Mỗi hình ảnh không được vượt quá 5 MB!");
        }

        String extension = getImageExtension(
                file.getOriginalFilename());

        String contentType = file.getContentType();

        String expectedContentType;

        switch (extension) {

            case ".jpg":
            case ".jpeg":
                expectedContentType = "image/jpeg";
                break;

            case ".png":
                expectedContentType = "image/png";
                break;

            case ".webp":
                expectedContentType = "image/webp";
                break;

            case ".gif":
                expectedContentType = "image/gif";
                break;

            default:
                throw new RuntimeException(
                        "Chỉ chấp nhận ảnh JPG, JPEG, PNG, WEBP hoặc GIF!");
        }

        if (contentType == null ||
                !expectedContentType.equalsIgnoreCase(contentType)) {

            throw new RuntimeException(
                    "Định dạng hình ảnh không khớp với loại file được tải lên!");
        }
    }

    // Lấy phần mở rộng của ảnh
    private String getImageExtension(String originalFilename) {

        if (originalFilename == null ||
                originalFilename.trim().isEmpty()) {

            throw new RuntimeException(
                    "Tên hình ảnh không hợp lệ!");
        }

        String fileName = new File(originalFilename).getName();

        int dotIndex = fileName.lastIndexOf('.');

        if (dotIndex < 0) {
            throw new RuntimeException(
                    "Hình ảnh phải có phần mở rộng hợp lệ!");
        }

        return fileName.substring(dotIndex).toLowerCase(Locale.ROOT);
    }

    // Xóa phòng
    @Transactional
    public void deleteRoom(Long id) {

        Room room = getRoomById(id);
        Long roomId = room.getId();

        // Kiểm tra hợp đồng, bao gồm cả hợp đồng đã thanh lý
        if (contractRepository.existsByRoom_Id(roomId)) {

            throw new RuntimeException(
                    "Không thể xóa phòng " + room.getRoomNumber() +
                    " vì phòng đang có lịch sử hợp đồng liên quan!");
        }

        // Kiểm tra khách thuê được gán với phòng
        if (tenantRepository.existsByRoom_Id(roomId)) {

            throw new RuntimeException(
                    "Không thể xóa phòng " + room.getRoomNumber() +
                    " vì vẫn còn khách thuê được gán với phòng này!");
        }

        // Kiểm tra hóa đơn
        if (invoiceRepository.existsByRoom_Id(roomId)) {

            throw new RuntimeException(
                    "Không thể xóa phòng " + room.getRoomNumber() +
                    " vì vẫn còn hóa đơn liên quan!");
        }

        roomRepository.delete(room);
    }

    // Lấy danh sách phòng trống dành cho trang công khai
    @Transactional(readOnly = true)
    public Page<Room> getAvailableRoomsForPublic(
            String keyword,
            Pageable pageable) {

        return getAvailableRoomsForPublic(
                keyword,
                null,
                null,
                null,
                null,
                pageable
        );
    }

    // Lấy danh sách phòng trống theo từ khóa, giá thuê và diện tích
    @Transactional(readOnly = true)
    public Page<Room> getAvailableRoomsForPublic(
            String keyword,
            Double minPrice,
            Double maxPrice,
            Double minArea,
            Double maxArea,
            Pageable pageable) {

        String searchKeyword = keyword == null
                ? null
                : keyword.trim();

        if (searchKeyword != null && searchKeyword.isEmpty()) {
            searchKeyword = null;
        }

        // Kiểm tra điều kiện giá thuê
        validateFilterValue(minPrice, "Giá thuê tối thiểu");
        validateFilterValue(maxPrice, "Giá thuê tối đa");

        if (minPrice != null && maxPrice != null
                && minPrice > maxPrice) {

            throw new IllegalArgumentException(
                    "Giá thuê tối thiểu không được lớn hơn giá thuê tối đa!");
        }

        // Kiểm tra điều kiện diện tích
        validateFilterValue(minArea, "Diện tích tối thiểu");
        validateFilterValue(maxArea, "Diện tích tối đa");

        if (minArea != null && maxArea != null
                && minArea > maxArea) {

            throw new IllegalArgumentException(
                    "Diện tích tối thiểu không được lớn hơn diện tích tối đa!");
        }

        return roomRepository.searchAvailableRooms(
                ROOM_AVAILABLE,
                searchKeyword,
                minPrice,
                maxPrice,
                minArea,
                maxArea,
                pageable
        );
    }

    // Kiểm tra giá trị bộ lọc hợp lệ
    private void validateFilterValue(Double value, String fieldName) {

        if (value != null &&
                (!Double.isFinite(value) || value < 0)) {

            throw new IllegalArgumentException(
                    fieldName + " phải là số hợp lệ và không được âm!");
        }
    }

    // Chuẩn hóa chuỗi
    private String trimToNull(String value) {

        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        return value.trim();
    }
}
