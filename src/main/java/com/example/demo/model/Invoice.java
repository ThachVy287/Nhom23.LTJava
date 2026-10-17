package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "invoices")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room; // Phòng nhận hóa đơn

    private String monthYear;        // Tháng/Năm (VD: "2026-10")
    
    private Double oldElectricity;   // Số điện cũ
    private Double newElectricity;   // Số điện mới
    private Double electricityPrice; // Đơn giá điện áp dụng
    
    private Double oldWater;         // Số nước cũ
    private Double newWater;         // Số nước mới
    private Double waterPrice;       // Đơn giá nước áp dụng

    private Double totalAmount;      // Tổng tiền (Tiền phòng + Tiền điện + Tiền nước)
    private String status;           // "CHUA_THANH_TOAN" hoặc "DA_THANH_TOAN"
}