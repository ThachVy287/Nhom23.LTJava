
package com.example.demo.repository;

import com.example.demo.model.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {

    // Kiểm tra phòng có hợp đồng đang hiệu lực hay không
    boolean existsByRoom_IdAndStatus(
            Long roomId,
            String status
    );

    // Kiểm tra hợp đồng khác đang sử dụng phòng
    // Không tính hợp đồng đang được chỉnh sửa
    boolean existsByRoom_IdAndStatusAndIdNot(
            Long roomId,
            String status,
            Long contractId
    );

    // Kiểm tra khách thuê có hợp đồng hay không
    boolean existsByTenant_Id(Long tenantId);

    // Kiểm tra khách thuê có hợp đồng ở trạng thái chỉ định
    boolean existsByTenant_IdAndStatus(
            Long tenantId,
            String status
    );

    // Kiểm tra khách thuê có hợp đồng hiệu lực khác
    boolean existsByTenant_IdAndStatusAndIdNot(
            Long tenantId,
            String status,
            Long contractId
    );

    // Kiểm tra phòng có hợp đồng hiệu lực thuộc khách thuê khác
    boolean existsByRoom_IdAndStatusAndTenant_IdNot(
            Long roomId,
            String status,
            Long tenantId
    );
    boolean existsByRoom_Id(Long roomId);
}
