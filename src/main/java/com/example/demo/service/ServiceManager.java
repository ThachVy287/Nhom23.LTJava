
package com.example.demo.service;

import com.example.demo.model.UtilityService;
import com.example.demo.repository.ServiceRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ServiceManager {

    @Autowired
    private ServiceRepository serviceRepository;

    // Lấy danh sách dịch vụ
    @Transactional(readOnly = true)
    public List<UtilityService> getAllServices() {
        return serviceRepository.findAll();
    }

    // Tìm dịch vụ theo ID
    @Transactional(readOnly = true)
    public UtilityService getServiceById(Long id) {

        if (id == null) {
            throw new RuntimeException("ID dịch vụ không hợp lệ!");
        }

        return serviceRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy dịch vụ có ID: " + id
                        ));
    }

    // Thêm mới hoặc cập nhật dịch vụ
    @Transactional
    public UtilityService saveService(UtilityService service) {

        if (service == null) {
            throw new RuntimeException(
                    "Thông tin dịch vụ không hợp lệ!"
            );
        }

        // Chuẩn hóa tên dịch vụ
        if (service.getServiceName() != null) {
            service.setServiceName(
                    service.getServiceName().trim()
            );
        }

        // Chuẩn hóa đơn vị tính
        if (service.getUnit() != null) {
            service.setUnit(
                    service.getUnit().trim()
            );
        }

        // Kiểm tra tên dịch vụ
        if (service.getServiceName() == null ||
                service.getServiceName().isBlank()) {

            throw new RuntimeException(
                    "Tên dịch vụ không được để trống!"
            );
        }

        // Kiểm tra đơn vị tính
        if (service.getUnit() == null ||
                service.getUnit().isBlank()) {

            throw new RuntimeException(
                    "Đơn vị tính không được để trống! " +
                    "Ví dụ: kWh, m³, tháng, người."
            );
        }

        // Kiểm tra đơn giá
        if (service.getPrice() == null ||
                !Double.isFinite(service.getPrice()) ||
                service.getPrice() < 0) {

            throw new RuntimeException(
                    "Đơn giá dịch vụ phải là số hợp lệ và không được âm!"
            );
        }

        // Kiểm tra ID khi cập nhật
        Long serviceId = service.getId();

        if (serviceId != null) {
            getServiceById(serviceId);
        }

        // Kiểm tra tên dịch vụ trùng
        boolean duplicateName;

        if (serviceId == null) {

            duplicateName =
                    serviceRepository.existsByServiceNameIgnoreCase(
                            service.getServiceName()
                    );

        } else {

            duplicateName =
                    serviceRepository.existsByServiceNameIgnoreCaseAndIdNot(
                            service.getServiceName(),
                            serviceId
                    );
        }

        if (duplicateName) {

            throw new RuntimeException(
                    "Tên dịch vụ \"" + service.getServiceName() +
                    "\" đã tồn tại trong hệ thống! " +
                    "Vui lòng nhập tên dịch vụ khác."
            );
        }

        return serviceRepository.save(service);
    }

    // Xóa dịch vụ
    @Transactional
    public void deleteService(Long id) {

        UtilityService service = getServiceById(id);

        serviceRepository.delete(service);
    }
}
