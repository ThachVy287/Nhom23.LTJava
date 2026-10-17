package com.example.demo.repository;

import com.example.demo.model.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long> {
    // Tìm kiếm khách thuê theo tên (gần đúng)
    List<Tenant> findByFullNameContainingIgnoreCase(String fullName);
}