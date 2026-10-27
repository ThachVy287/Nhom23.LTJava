package com.example.demo.repository;

import com.example.demo.model.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long> {
    
    List<Tenant> findByFullNameContainingIgnoreCase(String fullName);

    // Kiểm tra tồn tại theo Phone hoặc CCCD
    boolean existsByPhone(String phone);
    boolean existsByCccd(String cccd);

    // Kiểm tra trùng lặp ngoại trừ ID hiện tại (dùng cho lúc Update)
    boolean existsByPhoneAndIdNot(String phone, Long id);
    boolean existsByCccdAndIdNot(String cccd, Long id);
}