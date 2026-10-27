package com.example.demo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "rooms", uniqueConstraints = {
    @UniqueConstraint(columnNames = "roomNumber")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Số phòng không được để trống")
    @Column(unique = true, nullable = false)
    private String roomNumber;
    
    private Double price;
    private String status; // "TRONG" hoặc "DA_THUE"
    private String description;

    // --- CÁC THÔNG TIN BỔ SUNG CHO TRANG CHI TIẾT ---
    private Double roomArea;      // Diện tích phòng (m2)
    private Double bathroomArea;  // Diện tích phòng tắm (m2)
    private Boolean hasAc;        // Có máy lạnh hay không
    private Integer floorNumber;  // Số tầng
    private String imageUrls;     // Link hoặc tên file hình ảnh phòng
    private String bathroomDesc;  // Mô tả phòng tắm
}