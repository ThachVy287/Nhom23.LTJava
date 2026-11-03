package com.example.demo.repository;

import com.example.demo.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    // Kiểm tra phòng đã có hóa đơn trong tháng này chưa
    boolean existsByRoom_IdAndMonthYear(Long roomId, String monthYear);

    // Kiểm tra hóa đơn trùng tháng, không tính hóa đơn đang sửa
    boolean existsByRoom_IdAndMonthYearAndIdNot(
            Long roomId,
            String monthYear,
            Long invoiceId
    );

    // Tìm hóa đơn gần nhất trước tháng đang thao tác
    Optional<Invoice> findTopByRoom_IdAndMonthYearLessThanOrderByMonthYearDesc(
            Long roomId,
            String monthYear
    );

    // Tìm hóa đơn gần nhất sau tháng đang thao tác
    Optional<Invoice> findTopByRoom_IdAndMonthYearGreaterThanOrderByMonthYearAsc(
            Long roomId,
            String monthYear
    );

    // Tìm hóa đơn mới nhất của phòng theo tháng/năm
    Optional<Invoice> findTopByRoom_IdOrderByMonthYearDesc(Long roomId);

    // Kiểm tra phòng có hóa đơn ở tháng sau hay không
    boolean existsByRoom_IdAndMonthYearGreaterThan(
            Long roomId,
            String monthYear
    );
    boolean existsByRoom_Id(Long roomId);
}