package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "rooms")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String roomNumber;
    private Double price;
    private String status; // "TRONG" hoặc "DA_THUE"
    private String description;

    // --- CÁC THÔNG TIN BỔ SUNG CHO TRANG CHI TIẾT ---
    private Double roomArea;      // Diện tích phòng (m2), VD: 20.0
    private Double bathroomArea;  // Diện tích phòng tắm (m2), VD: 3.5
    private Boolean hasAc;        // Có máy lạnh hay không (true/false)
    private Integer floorNumber;  // Số tầng, VD: 1, 2, 3...
    private String imageUrl;      // Link hoặc tên file hình ảnh phòng, VD: /images/p101.jpg
    private String bathroomDesc;  // Mô tả phòng tắm, VD: "Phòng tắm khép kín, sạch sẽ, có vòi sen nóng lạnh"
}