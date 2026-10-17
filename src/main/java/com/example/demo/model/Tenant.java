package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tenants")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;   // Họ và tên khách thuê
    private String phone;      // Số điện thoại
    private String cccd;       // Số căn cước công dân
    private String email;      // Email liên hệ

    // Liên kết với phòng trọ (Mỗi phòng có thể có khách, hoặc khách thuê 1 phòng)
    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;
}