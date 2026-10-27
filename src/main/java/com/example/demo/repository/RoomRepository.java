package com.example.demo.repository;

import com.example.demo.model.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    
    Page<Room> findByRoomNumberContainingIgnoreCase(String roomNumber, Pageable pageable);
    Page<Room> findByStatus(String status, Pageable pageable);
    Page<Room> findByRoomNumberContainingIgnoreCaseAndStatus(String roomNumber, String status, Pageable pageable);

    boolean existsByRoomNumber(String roomNumber);
    boolean existsByRoomNumberAndIdNot(String roomNumber, Long id);

    // --- BỔ SUNG CHO TRANG CHỦ CÔNG KHAI (Tìm kiếm & Phân trang các phòng đang trống) ---
    Page<Room> findByStatusAndRoomNumberContainingIgnoreCase(String status, String roomNumber, Pageable pageable);
}