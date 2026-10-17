package com.example.demo.service;

import com.example.demo.model.Room;
import com.example.demo.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List; // Nhớ import thư viện List

@Service
public class RoomService {

    @Autowired
    private RoomRepository roomRepository;

    // Bổ sung phương thức này để lấy toàn bộ danh sách phòng (Dùng cho Dashboard và Tenant form)
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    // Lấy danh sách phòng có hỗ trợ Tìm kiếm, Lọc và Phân trang (Dùng cho trang rooms)
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

    // Lấy thông tin 1 phòng theo ID
    public Room getRoomById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng trọ có ID: " + id));
    }

    // Lưu hoặc cập nhật phòng trọ
    public Room saveRoom(Room room) {
        return roomRepository.save(room);
    }

    // Xóa phòng trọ theo ID
    public void deleteRoom(Long id) {
        roomRepository.deleteById(id);
    }
}