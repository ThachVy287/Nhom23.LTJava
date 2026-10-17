package com.example.demo.service;

import com.example.demo.model.UtilityService;
import com.example.demo.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServiceManager {

    @Autowired
    private ServiceRepository serviceRepository;

    public List<UtilityService> getAllServices() {
        return serviceRepository.findAll();
    }

    public UtilityService getServiceById(Long id) {
        return serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy dịch vụ ID: " + id));
    }

    public UtilityService saveService(UtilityService service) {
        return serviceRepository.save(service);
    }

    public void deleteService(Long id) {
        serviceRepository.deleteById(id);
    }
}