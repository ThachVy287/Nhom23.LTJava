package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Entity
@Table(name = "contracts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Liên kết với Phòng trọ
    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    // Liên kết với Khách thuê
    @ManyToOne
    @JoinColumn(name = "tenant_id")
    private Tenant tenant;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate; // Ngày bắt đầu thuê

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;   // Ngày kết thúc hợp đồng

    private Double deposit;      // Tiền cọc
    private Double monthlyRent;  // Giá thỏa thuận hàng tháng
    private String status;       // "DANG_THUE" hoặc "DA_THANH_LY"
}