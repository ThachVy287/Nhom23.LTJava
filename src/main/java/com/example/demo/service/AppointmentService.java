
package com.example.demo.service;

import com.example.demo.model.Appointment;
import com.example.demo.model.Room;
import com.example.demo.repository.AppointmentRepository;
import com.example.demo.repository.RoomRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class AppointmentService {

    private static final String WAITING = "CHO_XAC_NHAN";

    private static final List<String> VALID_STATUSES = List.of(
            "CHO_XAC_NHAN",
            "DA_XAC_NHAN",
            "DA_HOAN_THANH",
            "DA_HUY"
    );

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private RoomRepository roomRepository;

    // Đăng ký lịch xem phòng
    @Transactional
    public void registerAppointment(
            Long roomId,
            String fullName,
            String phone,
            LocalDate visitDate,
            LocalTime visitTime,
            String note) {

        // 1. Kiểm tra ID phòng
        if (roomId == null) {
            throw new IllegalArgumentException(
                    "Thông tin phòng không hợp lệ!");
        }

        // 2. Lấy phòng trong database
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Không tìm thấy phòng trọ!"
                        ));

        // 3. Chỉ cho phép đăng ký phòng còn trống
        if (!"TRONG".equals(room.getStatus())) {
            throw new IllegalArgumentException(
                    "Phòng này hiện đã có người thuê, không thể đăng ký xem!");
        }

        // 4. Kiểm tra họ tên
        String normalizedName = fullName == null
                ? ""
                : fullName.trim().replaceAll("\\s+", " ");

        if (normalizedName.length() < 2
                || normalizedName.length() > 100) {

            throw new IllegalArgumentException(
                    "Họ và tên phải có từ 2 đến 100 ký tự!");
        }

        // 5. Chuẩn hóa số điện thoại
        String normalizedPhone = phone == null
                ? ""
                : phone.trim().replaceAll("[\\s.-]", "");

        if (!normalizedPhone.matches("^(0\\d{9,10}|\\+84\\d{9,10})$")) {
            throw new IllegalArgumentException(
                    "Số điện thoại không đúng định dạng!");
        }

        // 6. Kiểm tra ngày xem phòng
        if (visitDate == null || visitDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Ngày xem phòng phải từ hôm nay trở đi!");
        }

        // 7. Kiểm tra giờ xem phòng
        if (visitTime == null
                || visitTime.isBefore(LocalTime.of(7, 0))
                || visitTime.isAfter(LocalTime.of(20, 0))) {

            throw new IllegalArgumentException(
                    "Giờ xem phòng phải nằm trong khoảng 07:00 đến 20:00!");
        }

        // 8. Kiểm tra ngày và giờ nếu khách chọn hôm nay
        if (visitDate.equals(LocalDate.now())
                && !visitTime.isAfter(LocalTime.now())) {

            throw new IllegalArgumentException(
                    "Giờ xem phòng phải lớn hơn thời gian hiện tại!");
        }

        // 9. Kiểm tra ghi chú
        String normalizedNote = note == null
                ? null
                : note.trim();

        if (normalizedNote != null && normalizedNote.isEmpty()) {
            normalizedNote = null;
        }

        if (normalizedNote != null && normalizedNote.length() > 500) {
            throw new IllegalArgumentException(
                    "Ghi chú không được vượt quá 500 ký tự!");
        }

        // 10. Kiểm tra đăng ký trùng
        boolean duplicated =
                appointmentRepository
                        .existsByRoom_IdAndPhoneAndVisitDateAndVisitTimeAndStatusNot(
                                roomId,
                                normalizedPhone,
                                visitDate,
                                visitTime,
                                "DA_HUY"
                        );

        if (duplicated) {
            throw new IllegalArgumentException(
                    "Bạn đã đăng ký xem phòng này vào thời gian đã chọn!");
        }

        // 11. Tạo lịch đăng ký
        Appointment appointment = new Appointment();

        appointment.setRoom(room);
        appointment.setFullName(normalizedName);
        appointment.setPhone(normalizedPhone);
        appointment.setVisitDate(visitDate);
        appointment.setVisitTime(visitTime);
        appointment.setNote(normalizedNote);
        appointment.setStatus(WAITING);

        appointmentRepository.save(appointment);
    }

    // Tìm kiếm lịch đăng ký
    @Transactional(readOnly = true)
    public Page<Appointment> getAppointments(
            String keyword,
            String status,
            Pageable pageable) {

        String searchKeyword = normalize(keyword);
        String searchStatus = normalize(status);

        if (!searchStatus.isEmpty()
                && !VALID_STATUSES.contains(searchStatus)) {

            throw new IllegalArgumentException(
                    "Trạng thái lịch đăng ký không hợp lệ!");
        }

        return appointmentRepository.searchAppointments(
                searchKeyword.isEmpty() ? null : searchKeyword,
                searchStatus.isEmpty() ? null : searchStatus,
                pageable
        );
    }

    // Lấy lịch đăng ký theo ID
    @Transactional(readOnly = true)
    public Appointment getAppointmentById(Long id) {

        if (id == null) {
            throw new IllegalArgumentException(
                    "ID lịch đăng ký không hợp lệ!");
        }

        return appointmentRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Không tìm thấy lịch đăng ký!"
                        ));
    }

    // Cập nhật trạng thái lịch đăng ký
    @Transactional
    public void updateAppointmentStatus(Long id, String status) {

        String normalizedStatus = normalize(status);

        if (!VALID_STATUSES.contains(normalizedStatus)) {
            throw new IllegalArgumentException(
                    "Trạng thái lịch đăng ký không hợp lệ!");
        }

        Appointment appointment = getAppointmentById(id);

        appointment.setStatus(normalizedStatus);

        appointmentRepository.save(appointment);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
