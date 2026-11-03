
package com.example.demo.repository;

import com.example.demo.model.UtilityService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceRepository extends JpaRepository<UtilityService, Long> {

    // Kiểm tra tên dịch vụ đã tồn tại, không phân biệt chữ hoa/thường
    boolean existsByServiceNameIgnoreCase(String serviceName);

    // Kiểm tra tên dịch vụ trùng với dịch vụ khác khi cập nhật
    boolean existsByServiceNameIgnoreCaseAndIdNot(
            String serviceName,
            Long id
    );
}
