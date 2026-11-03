
package com.example.demo.service;

import com.example.demo.model.Invoice;
import com.example.demo.model.Room;
import com.example.demo.model.UtilityService;
import com.example.demo.repository.InvoiceRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
public class InvoiceService {

    private static final String UNPAID = "CHUA_THANH_TOAN";
    private static final String PAID = "DA_THANH_TOAN";

    private static final String ELECTRICITY = "Điện";
    private static final String WATER = "Nước";

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private RoomService roomService;

    @Autowired
    private ServiceManager serviceManager;

    // Lấy danh sách hóa đơn
    @Transactional(readOnly = true)
    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }

    // Tìm hóa đơn theo ID
    @Transactional(readOnly = true)
    public Invoice getInvoiceById(Long id) {

        if (id == null) {
            throw new RuntimeException("ID hóa đơn không hợp lệ!");
        }

        return invoiceRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy hóa đơn ID: " + id
                        ));
    }

    // Lấy đơn giá dịch vụ hiện tại theo tên
    private Double getCurrentServicePrice(String serviceName) {

        List<UtilityService> services = serviceManager.getAllServices();

        UtilityService foundService = services.stream()
                .filter(service ->
                        service.getServiceName() != null &&
                        service.getServiceName().trim()
                                .equalsIgnoreCase(serviceName)
                )
                .findFirst()
                .orElseThrow(() ->
                        new RuntimeException(
                                "Chưa cấu hình dịch vụ " + serviceName +
                                " trong hệ thống! Vui lòng vào Quản lý dịch vụ " +
                                "để thêm dịch vụ trước khi lập hóa đơn."
                        ));

        Double price = foundService.getPrice();

        if (price == null ||
                !Double.isFinite(price) ||
                price < 0) {

            throw new RuntimeException(
                    "Đơn giá dịch vụ " + serviceName +
                    " không hợp lệ! Vui lòng kiểm tra lại trong Quản lý dịch vụ."
            );
        }

        return price;
    }

    // Đồng bộ đơn giá dịch vụ với hóa đơn
    private void synchronizeServicePrices(
            Invoice invoice,
            Invoice existingInvoice) {

        if (existingInvoice == null) {

            // HÓA ĐƠN MỚI:
            // Lấy đơn giá hiện tại từ bảng services.
            invoice.setElectricityPrice(
                    getCurrentServicePrice(ELECTRICITY)
            );

            invoice.setWaterPrice(
                    getCurrentServicePrice(WATER)
            );

        } else {

            // HÓA ĐƠN CŨ:
            // Giữ nguyên đơn giá lịch sử đã lưu.
            // Không lấy đơn giá mới ghi đè hóa đơn cũ.

            Double oldElectricityPrice =
                    existingInvoice.getElectricityPrice();

            Double oldWaterPrice =
                    existingInvoice.getWaterPrice();

            // Nếu dữ liệu hóa đơn cũ thiếu đơn giá,
            // mới lấy đơn giá hiện tại để bổ sung.
            if (oldElectricityPrice == null) {
                oldElectricityPrice =
                        getCurrentServicePrice(ELECTRICITY);
            }

            if (oldWaterPrice == null) {
                oldWaterPrice =
                        getCurrentServicePrice(WATER);
            }

            invoice.setElectricityPrice(oldElectricityPrice);
            invoice.setWaterPrice(oldWaterPrice);
        }
    }

    // Thêm mới hoặc cập nhật hóa đơn
    @Transactional
    public Invoice saveInvoice(Invoice invoice) {

        if (invoice == null) {
            throw new RuntimeException(
                    "Thông tin hóa đơn không hợp lệ!"
            );
        }

        // 1. Kiểm tra phòng
        if (invoice.getRoom() == null ||
                invoice.getRoom().getId() == null) {

            throw new RuntimeException(
                    "Vui lòng chọn phòng trọ!"
            );
        }

        Long roomId = invoice.getRoom().getId();

        // Lấy phòng thực tế từ database
        Room room = roomService.getRoomById(roomId);

        if (room == null) {
            throw new RuntimeException(
                    "Không tìm thấy phòng trọ!"
            );
        }

        // 2. Kiểm tra tháng/năm
        if (invoice.getMonthYear() == null ||
                invoice.getMonthYear().isBlank()) {

            throw new RuntimeException(
                    "Vui lòng chọn tháng lập hóa đơn!"
            );
        }

        String monthYear = invoice.getMonthYear().trim();

        try {
            YearMonth.parse(monthYear);
        } catch (DateTimeParseException e) {

            throw new RuntimeException(
                    "Tháng hóa đơn không hợp lệ! " +
                    "Định dạng đúng là yyyy-MM."
            );
        }

        invoice.setMonthYear(monthYear);

        // 3. Kiểm tra hóa đơn khi cập nhật
        Invoice existingInvoice = null;

        if (invoice.getId() != null) {

            existingInvoice = getInvoiceById(invoice.getId());

            Long oldRoomId = existingInvoice.getRoom() != null
                    ? existingInvoice.getRoom().getId()
                    : null;

            // Không cho chuyển hóa đơn sang phòng khác
            if (oldRoomId == null ||
                    !oldRoomId.equals(roomId)) {

                throw new RuntimeException(
                        "Không thể thay đổi phòng của hóa đơn đã tạo!"
                );
            }
        }

        // 4. Kiểm tra hóa đơn trùng phòng và tháng
        boolean duplicated;

        if (invoice.getId() == null) {

            duplicated =
                    invoiceRepository.existsByRoom_IdAndMonthYear(
                            roomId, monthYear
                    );

        } else {

            duplicated =
                    invoiceRepository
                            .existsByRoom_IdAndMonthYearAndIdNot(
                                    roomId,
                                    monthYear,
                                    invoice.getId()
                            );
        }

        if (duplicated) {

            throw new RuntimeException(
                    "Phòng này đã có hóa đơn trong tháng " +
                    monthYear + "!"
            );
        }

        // 5. TỰ ĐỘNG ĐỒNG BỘ ĐƠN GIÁ ĐIỆN VÀ NƯỚC
        synchronizeServicePrices(invoice, existingInvoice);

        // 6. Kiểm tra các chỉ số và đơn giá
        validateNonNegative(
                invoice.getOldElectricity(),
                "Số điện cũ"
        );

        validateNonNegative(
                invoice.getNewElectricity(),
                "Số điện mới"
        );

        validateNonNegative(
                invoice.getElectricityPrice(),
                "Đơn giá điện"
        );

        validateNonNegative(
                invoice.getOldWater(),
                "Số nước cũ"
        );

        validateNonNegative(
                invoice.getNewWater(),
                "Số nước mới"
        );

        validateNonNegative(
                invoice.getWaterPrice(),
                "Đơn giá nước"
        );

        // Các trường bắt buộc
        if (invoice.getOldElectricity() == null ||
                invoice.getNewElectricity() == null ||
                invoice.getElectricityPrice() == null ||
                invoice.getOldWater() == null ||
                invoice.getNewWater() == null ||
                invoice.getWaterPrice() == null) {

            throw new RuntimeException(
                    "Vui lòng nhập đầy đủ chỉ số cũ, chỉ số mới " +
                    "và thông tin điện nước!"
            );
        }

        // 7. Chỉ số mới không được nhỏ hơn chỉ số cũ
        if (invoice.getNewElectricity() <
                invoice.getOldElectricity()) {

            throw new RuntimeException(
                    "Số điện mới không được thấp hơn số điện cũ!"
            );
        }

        if (invoice.getNewWater() <
                invoice.getOldWater()) {

            throw new RuntimeException(
                    "Số nước mới không được thấp hơn số nước cũ!"
            );
        }

        // 8. Kiểm tra hóa đơn của tháng trước
        Invoice previousInvoice = invoiceRepository
                .findTopByRoom_IdAndMonthYearLessThanOrderByMonthYearDesc(
                        roomId, monthYear
                )
                .orElse(null);

        if (previousInvoice != null) {

            if (previousInvoice.getNewElectricity() == null ||
                    previousInvoice.getNewWater() == null) {

                throw new RuntimeException(
                        "Hóa đơn tháng trước thiếu chỉ số điện hoặc nước!"
                );
            }

            if (!sameNumber(
                    invoice.getOldElectricity(),
                    previousInvoice.getNewElectricity())) {

                throw new RuntimeException(
                        "Số điện cũ phải bằng số điện mới của " +
                        "hóa đơn tháng trước (" +
                        previousInvoice.getNewElectricity() + ")!"
                );
            }

            if (!sameNumber(
                    invoice.getOldWater(),
                    previousInvoice.getNewWater())) {

                throw new RuntimeException(
                        "Số nước cũ phải bằng số nước mới của " +
                        "hóa đơn tháng trước (" +
                        previousInvoice.getNewWater() + ")!"
                );
            }
        }

        // 9. Kiểm tra hóa đơn của tháng sau
        Invoice nextInvoice = invoiceRepository
                .findTopByRoom_IdAndMonthYearGreaterThanOrderByMonthYearAsc(
                        roomId, monthYear
                )
                .orElse(null);

        if (nextInvoice != null) {

            if (nextInvoice.getOldElectricity() == null ||
                    nextInvoice.getOldWater() == null) {

                throw new RuntimeException(
                        "Hóa đơn tháng sau thiếu chỉ số điện hoặc nước!"
                );
            }

            if (!sameNumber(
                    invoice.getNewElectricity(),
                    nextInvoice.getOldElectricity())) {

                throw new RuntimeException(
                        "Số điện mới phải bằng số điện cũ của " +
                        "hóa đơn tháng sau (" +
                        nextInvoice.getOldElectricity() + ")!"
                );
            }

            if (!sameNumber(
                    invoice.getNewWater(),
                    nextInvoice.getOldWater())) {

                throw new RuntimeException(
                        "Số nước mới phải bằng số nước cũ của " +
                        "hóa đơn tháng sau (" +
                        nextInvoice.getOldWater() + ")!"
                );
            }
        }

        // 10. Lấy giá phòng
        if (room.getPrice() != null &&
                (!Double.isFinite(room.getPrice()) ||
                        room.getPrice() < 0)) {

            throw new RuntimeException(
                    "Giá phòng không hợp lệ!"
            );
        }

        BigDecimal roomPrice =
                toBigDecimal(room.getPrice());

        // 11. Tính tiền điện
        BigDecimal electricityUsed =
                toBigDecimal(invoice.getNewElectricity())
                        .subtract(
                                toBigDecimal(invoice.getOldElectricity())
                        );

        BigDecimal electricityTotal =
                electricityUsed.multiply(
                        toBigDecimal(invoice.getElectricityPrice())
                );

        // 12. Tính tiền nước
        BigDecimal waterUsed =
                toBigDecimal(invoice.getNewWater())
                        .subtract(
                                toBigDecimal(invoice.getOldWater())
                        );

        BigDecimal waterTotal =
                waterUsed.multiply(
                        toBigDecimal(invoice.getWaterPrice())
                );

        // 13. Tính tổng tiền
        // Tổng tiền = Tiền phòng + Tiền điện + Tiền nước
        BigDecimal totalAmount = roomPrice
                .add(electricityTotal)
                .add(waterTotal)
                .setScale(2, RoundingMode.HALF_UP);

        invoice.setTotalAmount(totalAmount.doubleValue());

        // 14. Kiểm tra trạng thái hóa đơn
        if (invoice.getStatus() == null ||
                invoice.getStatus().isBlank()) {

            if (existingInvoice != null) {

                invoice.setStatus(existingInvoice.getStatus());

            } else {

                invoice.setStatus(UNPAID);
            }
        }

        if (!UNPAID.equals(invoice.getStatus()) &&
                !PAID.equals(invoice.getStatus())) {

            throw new RuntimeException(
                    "Trạng thái hóa đơn không hợp lệ!"
            );
        }

        // 15. Lưu hóa đơn
        return invoiceRepository.save(invoice);
    }

    // Xóa hóa đơn
    @Transactional
    public void deleteInvoice(Long id) {

        Invoice invoice = getInvoiceById(id);

        if (invoice.getRoom() == null ||
                invoice.getRoom().getId() == null) {

            throw new RuntimeException(
                    "Không thể xác định phòng của hóa đơn!"
            );
        }

        Long roomId = invoice.getRoom().getId();
        String monthYear = invoice.getMonthYear();

        // Không cho xóa hóa đơn cũ nếu đã có hóa đơn tháng sau
        boolean hasNextInvoice =
                invoiceRepository.existsByRoom_IdAndMonthYearGreaterThan(
                        roomId, monthYear
                );

        if (hasNextInvoice) {

            throw new RuntimeException(
                    "Không thể xóa hóa đơn tháng " + monthYear +
                    " vì phòng đã có hóa đơn ở tháng sau. " +
                    "Việc xóa có thể làm sai chuỗi chỉ số điện nước!"
            );
        }

        invoiceRepository.delete(invoice);
    }

    // Kiểm tra số không âm và hợp lệ
    private void validateNonNegative(
            Double value,
            String fieldName) {

        if (value != null &&
                (!Double.isFinite(value) || value < 0)) {

            throw new RuntimeException(
                    fieldName +
                    " phải là số hợp lệ và không được âm!"
            );
        }
    }

    // Chuyển Double sang BigDecimal
    private BigDecimal toBigDecimal(Double value) {

        if (value == null) {
            return BigDecimal.ZERO;
        }

        return BigDecimal.valueOf(value);
    }

    // So sánh hai giá trị số
    private boolean sameNumber(
            Double first,
            Double second) {

        if (first == null || second == null) {
            return false;
        }

        return BigDecimal.valueOf(first)
                .compareTo(BigDecimal.valueOf(second)) == 0;
    }
}
