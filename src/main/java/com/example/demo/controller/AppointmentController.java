
package com.example.demo.controller;

import com.example.demo.model.Room;
import com.example.demo.service.AppointmentService;
import com.example.demo.service.RoomService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

@Controller
@RequestMapping("/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private RoomService roomService;

    // Mở form đăng ký xem phòng
    @GetMapping("/register")
    public String showRegisterForm(
            @RequestParam Long roomId,
            Model model) {

        Room room = roomService.getRoomById(roomId);

        model.addAttribute("room", room);
        model.addAttribute("roomId", roomId);
        model.addAttribute("minDate", LocalDate.now());
        model.addAttribute("today", LocalDate.now().toString());

        return "appointments/register";
    }

    // Nhận thông tin đăng ký từ khách hàng
    @PostMapping("/register")
    public String registerAppointment(
            @RequestParam Long roomId,
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String visitDate,
            @RequestParam(required = false) String visitTime,
            @RequestParam(required = false) String note,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {

            // Chuyển ngày và giờ từ form sang kiểu dữ liệu Java
            LocalDate date;
            LocalTime time;

            try {
                date = LocalDate.parse(
                        visitDate == null ? "" : visitDate.trim());

                time = LocalTime.parse(
                        visitTime == null ? "" : visitTime.trim());

            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException(
                        "Ngày hoặc giờ xem phòng không đúng định dạng!");
            }

            // Gọi Service để kiểm tra và lưu đăng ký
            appointmentService.registerAppointment(
                    roomId,
                    fullName,
                    phone,
                    date,
                    time,
                    note
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Đăng ký lịch xem phòng thành công! " +
                    "Chủ trọ sẽ liên hệ để xác nhận lịch của bạn."
            );

            return "redirect:/appointments/success";

        } catch (IllegalArgumentException e) {

            // Nếu có lỗi, giữ lại dữ liệu khách hàng đã nhập
            model.addAttribute("errorMessage", e.getMessage());

            model.addAttribute("room", roomService.getRoomById(roomId));
            model.addAttribute("roomId", roomId);
            model.addAttribute("minDate", LocalDate.now());

            model.addAttribute("fullName", fullName);
            model.addAttribute("phone", phone);
            model.addAttribute("visitDate", visitDate);
            model.addAttribute("visitTime", visitTime);
            model.addAttribute("note", note);

            model.addAttribute("today", LocalDate.now().toString());

            return "appointments/register";

        }
    }

    // Trang thông báo đăng ký thành công
    @GetMapping("/success")
    public String registrationSuccess() {
        return "appointments/success";
    }
}
