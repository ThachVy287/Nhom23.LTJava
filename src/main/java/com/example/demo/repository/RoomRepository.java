
package com.example.demo.repository;

import com.example.demo.model.Room;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    // Tìm kiếm và lọc phòng trong trang quản trị
    Page<Room> findByRoomNumberContainingIgnoreCase(
            String roomNumber, Pageable pageable);

    Page<Room> findByStatus(
            String status, Pageable pageable);

    Page<Room> findByRoomNumberContainingIgnoreCaseAndStatus(
            String roomNumber, String status, Pageable pageable);

    // Kiểm tra số phòng trùng
    boolean existsByRoomNumber(String roomNumber);

    boolean existsByRoomNumberAndIdNot(String roomNumber, Long id);

    // Kiểm tra số phòng trùng không phân biệt chữ hoa và chữ thường
    boolean existsByRoomNumberIgnoreCase(String roomNumber);

    boolean existsByRoomNumberIgnoreCaseAndIdNot(
            String roomNumber, Long id);

    // Tìm kiếm phòng trống trên trang công khai theo nhiều điều kiện
    @Query(
        value = "SELECT r FROM Room r " +
                "WHERE r.status = :status " +
                "AND (:keyword IS NULL OR " +
                "LOWER(r.roomNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
                "AND (:minPrice IS NULL OR r.price >= :minPrice) " +
                "AND (:maxPrice IS NULL OR r.price <= :maxPrice) " +
                "AND (:minArea IS NULL OR r.roomArea >= :minArea) " +
                "AND (:maxArea IS NULL OR r.roomArea <= :maxArea)",

        countQuery = "SELECT COUNT(r) FROM Room r " +
                "WHERE r.status = :status " +
                "AND (:keyword IS NULL OR " +
                "LOWER(r.roomNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
                "AND (:minPrice IS NULL OR r.price >= :minPrice) " +
                "AND (:maxPrice IS NULL OR r.price <= :maxPrice) " +
                "AND (:minArea IS NULL OR r.roomArea >= :minArea) " +
                "AND (:maxArea IS NULL OR r.roomArea <= :maxArea)"
    )
    Page<Room> searchAvailableRooms(
            @Param("status") String status,
            @Param("keyword") String keyword,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("minArea") Double minArea,
            @Param("maxArea") Double maxArea,
            Pageable pageable
    );

    // Giữ lại phương thức cũ để không ảnh hưởng những nơi đang sử dụng
    Page<Room> findByStatusAndRoomNumberContainingIgnoreCase(
            String status, String roomNumber, Pageable pageable);

    // Kiểm tra quan hệ phòng khi xóa
    boolean existsById(Long id);
}
