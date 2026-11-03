
package com.example.demo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(
    name = "appointments",
    indexes = {
        @Index(name = "idx_appointment_room", columnList = "room_id"),
        @Index(name = "idx_appointment_status", columnList = "status"),
        @Index(name = "idx_appointment_date", columnList = "visit_date")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Một phòng có thể có nhiều lịch đăng ký xem
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @NotBlank(message = "Vui lòng nhập họ và tên")
    @Column(nullable = false, length = 100)
    private String fullName;

    @NotBlank(message = "Vui lòng nhập số điện thoại")
    @Column(nullable = false, length = 20)
    private String phone;

    @NotNull(message = "Vui lòng chọn ngày xem phòng")
    @Column(name = "visit_date", nullable = false)
    private LocalDate visitDate;

    @NotNull(message = "Vui lòng chọn giờ xem phòng")
    @Column(name = "visit_time", nullable = false)
    private LocalTime visitTime;

    @Column(length = 500)
    private String note;

    // CHO_XAC_NHAN, DA_XAC_NHAN, DA_HOAN_THANH, DA_HUY
    @Column(nullable = false, length = 30)
    private String status = "CHO_XAC_NHAN";

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (status == null || status.isBlank()) {
            status = "CHO_XAC_NHAN";
        }
    }
}
