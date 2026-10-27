package com.example.demo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tenants", uniqueConstraints = {
    @UniqueConstraint(columnNames = "phone"),
    @UniqueConstraint(columnNames = "cccd")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Họ và tên không được để trống")
    private String fullName; 

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^0\\d{9}$", message = "Số điện thoại phải bắt đầu bằng số 0 và có đúng 10 chữ số")
    @Column(unique = true, nullable = false)
    private String phone;     

    @NotBlank(message = "CCCD không được để trống")
    @Pattern(regexp = "^(\\d{9}|\\d{12})$", message = "CCCD phải có đúng 9 hoặc 12 chữ số")
    @Column(unique = true, nullable = false)
    private String cccd;       

    private String email;      

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;
}