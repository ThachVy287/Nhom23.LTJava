package com.example.demo.service;

import com.example.demo.model.Tenant;
import com.example.demo.repository.TenantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TenantService {

    @Autowired
    private TenantRepository tenantRepository;

    public List<Tenant> getAllTenants() {
        return tenantRepository.findAll();
    }

    public Tenant getTenantById(Long id) {
        return tenantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách thuê ID: " + id));
    }

    public Tenant saveTenant(Tenant tenant) {
        // Trường hợp THÊM MỚI (id == null)
        if (tenant.getId() == null) {
            if (tenantRepository.existsByPhone(tenant.getPhone())) {
                throw new RuntimeException("Số điện thoại này đã tồn tại trong hệ thống!");
            }
            if (tenantRepository.existsByCccd(tenant.getCccd())) {
                throw new RuntimeException("Số CCCD này đã tồn tại trong hệ thống!");
            }
        } 
        // Trường hợp CẬP NHẬT (id != null)
        else {
            if (tenantRepository.existsByPhoneAndIdNot(tenant.getPhone(), tenant.getId())) {
                throw new RuntimeException("Số điện thoại này đã được sử dụng bởi khách thuê khác!");
            }
            if (tenantRepository.existsByCccdAndIdNot(tenant.getCccd(), tenant.getId())) {
                throw new RuntimeException("Số CCCD này đã được sử dụng bởi khách thuê khác!");
            }
        }

        return tenantRepository.save(tenant);
    }

    public void deleteTenant(Long id) {
        tenantRepository.deleteById(id);
    }
}