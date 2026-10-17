package com.example.demo.repository;

import com.example.demo.model.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    
    // Tìm kiếm theo số phòng (gần đúng) có phân trang
    Page<Room> findByRoomNumberContainingIgnoreCase(String roomNumber, Pageable pageable);

    // Lọc theo trạng thái có phân trang
    Page<Room> findByStatus(String status, Pageable pageable);

    // Vừa tìm kiếm theo tên vừa lọc theo trạng thái có phân trang
    Page<Room> findByRoomNumberContainingIgnoreCaseAndStatus(String roomNumber, String status, Pageable pageable);
}