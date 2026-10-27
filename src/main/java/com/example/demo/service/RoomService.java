package com.example.demo.service;

import com.example.demo.model.Room;
import com.example.demo.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class RoomService {

    @Autowired
    private RoomRepository roomRepository;

    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public Page<Room> getRooms(String keyword, String status, Pageable pageable) {
        if (keyword != null && !keyword.isEmpty() && status != null && !status.isEmpty()) {
            return roomRepository.findByRoomNumberContainingIgnoreCaseAndStatus(keyword, status, pageable);
        } else if (keyword != null && !keyword.isEmpty()) {
            return roomRepository.findByRoomNumberContainingIgnoreCase(keyword, pageable);
        } else if (status != null && !status.isEmpty()) {
            return roomRepository.findByStatus(status, pageable);
        } else {
            return roomRepository.findAll(pageable);
        }
    }

    public Room getRoomById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng trọ có ID: " + id));
    }

    // Xử lý lưu phòng kèm upload nhiều hình ảnh và kiểm tra trùng lặp số phòng
    public void saveRoomWithImages(Room room, MultipartFile[] imageFiles) throws IOException {
        
        // --- KIỂM TRA RÀNG BUỘC TRÙNG SỐ PHÒNG ---
        if (room.getId() == null) {
            // Thêm mới
            if (roomRepository.existsByRoomNumber(room.getRoomNumber())) {
                throw new RuntimeException("Số phòng '" + room.getRoomNumber() + "' đã tồn tại trong hệ thống!");
            }
        } else {
            // Cập nhật
            if (roomRepository.existsByRoomNumberAndIdNot(room.getRoomNumber(), room.getId())) {
                throw new RuntimeException("Số phòng '" + room.getRoomNumber() + "' đã thuộc về một phòng khác!");
            }
        }

        List<String> pathList = new ArrayList<>();
        
        // 1. Giữ lại các ảnh cũ mà người dùng chưa bấm xóa
        if (room.getImageUrls() != null && !room.getImageUrls().isEmpty()) {
            for (String img : room.getImageUrls().split(",")) {
                if (!img.trim().isEmpty()) {
                    pathList.add(img.trim());
                }
            }
        }

        // 2. Tải lên và thêm các ảnh mới chọn từ thư mục máy tính
        String uploadDir = "src/main/resources/static/images/";
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        if (imageFiles != null && imageFiles.length > 0) {
            for (MultipartFile file : imageFiles) {
                if (!file.isEmpty()) {
                    String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                    Path filePath = Paths.get(uploadDir + fileName);
                    Files.write(filePath, file.getBytes());
                    pathList.add("/images/" + fileName);
                }
            }
        }

        // Gộp lại thành chuỗi phân cách bằng dấu phẩy lưu vào database
        room.setImageUrls(String.join(",", pathList));
        roomRepository.save(room);
    }

    public void deleteRoom(Long id) {
        roomRepository.deleteById(id);
    }
    
    public Page<Room> getAvailableRoomsForPublic(String keyword, Pageable pageable) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return roomRepository.findByStatusAndRoomNumberContainingIgnoreCase("TRONG", keyword.trim(), pageable);
        } else {
            return roomRepository.findByStatus("TRONG", pageable);
        }
    }
}