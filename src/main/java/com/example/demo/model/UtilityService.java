package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "services")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UtilityService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String serviceName; // Tên dịch vụ (Điện, Nước, Internet...)
    private Double price;       // Đơn giá (VNĐ)
    private String unit;        // Đơn vị tính (kWh, m3, tháng, người...)
}