
package com.example.demo.repository;

import com.example.demo.model.Appointment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;

@Repository
public interface AppointmentRepository
        extends JpaRepository<Appointment, Long> {

    // Kiểm tra khách đã đăng ký cùng phòng, ngày và giờ hay chưa
    boolean existsByRoom_IdAndPhoneAndVisitDateAndVisitTimeAndStatusNot(
            Long roomId,
            String phone,
            LocalDate visitDate,
            LocalTime visitTime,
            String status
    );

    // Tìm kiếm lịch đăng ký trong trang quản trị
    @Query(
        value = """
            SELECT a FROM Appointment a
            JOIN a.room r
            WHERE
                (
                    :keyword IS NULL
                    OR LOWER(a.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR a.phone LIKE CONCAT('%', :keyword, '%')
                    OR LOWER(r.roomNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))
                )
                AND (:status IS NULL OR a.status = :status)
            """,
        countQuery = """
            SELECT COUNT(a) FROM Appointment a
            JOIN a.room r
            WHERE
                (
                    :keyword IS NULL
                    OR LOWER(a.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR a.phone LIKE CONCAT('%', :keyword, '%')
                    OR LOWER(r.roomNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))
                )
                AND (:status IS NULL OR a.status = :status)
            """
    )
    Page<Appointment> searchAppointments(
            @Param("keyword") String keyword,
            @Param("status") String status,
            Pageable pageable
    );
}
