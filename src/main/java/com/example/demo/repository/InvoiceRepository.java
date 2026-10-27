package com.example.demo.repository;

import com.example.demo.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    // Tìm hóa đơn mới nhất của phòng dựa theo ID phòng (sắp xếp giảm dần theo ID hoặc tháng năm)
    Optional<Invoice> findTopByRoomIdOrderByIdDesc(Long roomId);
}