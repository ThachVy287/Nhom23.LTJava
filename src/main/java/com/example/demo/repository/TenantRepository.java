
package com.example.demo.repository;

import com.example.demo.model.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long> {

    // Tìm kiếm khách thuê theo họ tên
    List<Tenant> findByFullNameContainingIgnoreCase(String fullName);

    // Kiểm tra số điện thoại và CCCD trùng
    boolean existsByPhone(String phone);

    boolean existsByCccd(String cccd);

    // Kiểm tra trùng ngoại trừ khách thuê đang cập nhật
    boolean existsByPhoneAndIdNot(String phone, Long id);

    boolean existsByCccdAndIdNot(String cccd, Long id);

    // Kiểm tra phòng đã được gán cho khách thuê nào chưa
    boolean existsByRoom_Id(Long roomId);

    // Kiểm tra phòng được gán cho khách thuê khác
    boolean existsByRoom_IdAndIdNot(Long roomId, Long tenantId);
}
