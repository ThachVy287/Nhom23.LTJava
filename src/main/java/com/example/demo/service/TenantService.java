
package com.example.demo.service;

import com.example.demo.model.Room;
import com.example.demo.model.Tenant;
import com.example.demo.repository.ContractRepository;
import com.example.demo.repository.RoomRepository;
import com.example.demo.repository.TenantRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TenantService {

    private static final String ROOM_AVAILABLE = "TRONG";
    private static final String ROOM_RENTED = "DA_THUE";
    private static final String ACTIVE_CONTRACT = "Đang hiệu lực";

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private RoomRepository roomRepository;

    // Lấy danh sách khách thuê
    @Transactional(readOnly = true)
    public List<Tenant> getAllTenants() {
        return tenantRepository.findAll();
    }

    // Tìm khách thuê theo ID
    @Transactional(readOnly = true)
    public Tenant getTenantById(Long id) {

        if (id == null) {
            throw new RuntimeException(
                    "ID khách thuê không hợp lệ!"
            );
        }

        return tenantRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy khách thuê ID: " + id
                        ));
    }

    // Thêm mới hoặc cập nhật khách thuê
    @Transactional
    public Tenant saveTenant(Tenant formTenant) {

        if (formTenant == null) {
            throw new RuntimeException(
                    "Thông tin khách thuê không hợp lệ!"
            );
        }

        // -------------------------------------------------
        // 1. CHUẨN HÓA DỮ LIỆU
        // -------------------------------------------------

        if (formTenant.getFullName() != null) {
            formTenant.setFullName(formTenant.getFullName().trim());
        }

        if (formTenant.getPhone() != null) {
            formTenant.setPhone(formTenant.getPhone().trim());
        }

        if (formTenant.getCccd() != null) {
            formTenant.setCccd(formTenant.getCccd().trim());
        }

        if (formTenant.getEmail() != null) {

            formTenant.setEmail(formTenant.getEmail().trim());

            if (formTenant.getEmail().isEmpty()) {
                formTenant.setEmail(null);
            }
        }

        // -------------------------------------------------
        // 2. KIỂM TRA HỌ TÊN
        // -------------------------------------------------

        if (formTenant.getFullName() == null ||
                formTenant.getFullName().isBlank()) {

            throw new RuntimeException(
                    "Họ và tên không được để trống!"
            );
        }

        // -------------------------------------------------
        // 3. KIỂM TRA SỐ ĐIỆN THOẠI
        // -------------------------------------------------

        if (formTenant.getPhone() == null ||
                !formTenant.getPhone().matches("^0\\d{9}$")) {

            throw new RuntimeException(
                    "Số điện thoại phải bắt đầu bằng số 0 và có đúng 10 chữ số!"
            );
        }

        // -------------------------------------------------
        // 4. KIỂM TRA CCCD
        // -------------------------------------------------

        if (formTenant.getCccd() == null ||
                !formTenant.getCccd().matches("^(\\d{9}|\\d{12})$")) {

            throw new RuntimeException(
                    "CCCD phải có đúng 9 hoặc 12 chữ số!"
            );
        }

        Long tenantId = formTenant.getId();

        // -------------------------------------------------
        // 5. LẤY KHÁCH THUÊ ĐANG ĐƯỢC QUẢN LÝ
        // -------------------------------------------------

        Tenant tenant;

        if (tenantId != null) {

            // Lấy đối tượng thật từ database
            tenant = getTenantById(tenantId);

        } else {

            // Khách thuê mới
            tenant = new Tenant();
        }

        // Lưu ID phòng cũ
        Long oldRoomId = tenant.getRoom() != null
                ? tenant.getRoom().getId()
                : null;

        // Lấy ID phòng được chọn từ form
        Long newRoomId = formTenant.getRoom() != null
                ? formTenant.getRoom().getId()
                : null;

        // -------------------------------------------------
        // 6. KIỂM TRA TRÙNG SỐ ĐIỆN THOẠI
        // -------------------------------------------------

        boolean duplicatePhone = tenantId == null
                ? tenantRepository.existsByPhone(formTenant.getPhone())
                : tenantRepository.existsByPhoneAndIdNot(
                        formTenant.getPhone(),
                        tenantId
                );

        if (duplicatePhone) {

            throw new RuntimeException(
                    "Số điện thoại này đã được sử dụng bởi khách thuê khác!"
            );
        }

        // -------------------------------------------------
        // 7. KIỂM TRA TRÙNG CCCD
        // -------------------------------------------------

        boolean duplicateCccd = tenantId == null
                ? tenantRepository.existsByCccd(formTenant.getCccd())
                : tenantRepository.existsByCccdAndIdNot(
                        formTenant.getCccd(),
                        tenantId
                );

        if (duplicateCccd) {

            throw new RuntimeException(
                    "Số CCCD này đã được sử dụng bởi khách thuê khác!"
            );
        }

        // -------------------------------------------------
        // 8. KIỂM TRA THAY ĐỔI PHÒNG
        // -------------------------------------------------

        boolean changingRoom = oldRoomId == null
                ? newRoomId != null
                : !oldRoomId.equals(newRoomId);

        // Không cho đổi hoặc bỏ phòng khi khách còn hợp đồng hiệu lực
        if (tenantId != null &&
                changingRoom &&
                contractRepository.existsByTenant_IdAndStatus(
                        tenantId,
                        ACTIVE_CONTRACT
                )) {

            throw new RuntimeException(
                    "Khách thuê đang có hợp đồng hiệu lực! " +
                    "Không thể thay đổi phòng trực tiếp. " +
                    "Hãy xử lý hợp đồng trước."
            );
        }

        // -------------------------------------------------
        // 9. XỬ LÝ PHÒNG ĐƯỢC GÁN
        // -------------------------------------------------

        if (newRoomId != null) {

            // Lấy đối tượng phòng thực tế từ database
            Room selectedRoom = roomRepository.findById(newRoomId)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Phòng trọ không tồn tại!"
                            ));

            boolean sameRoom = oldRoomId != null &&
                    oldRoomId.equals(newRoomId);

            // Kiểm tra phòng đã được khách thuê khác gán chưa
            boolean assignedToOtherTenant = tenantId == null
                    ? tenantRepository.existsByRoom_Id(newRoomId)
                    : tenantRepository.existsByRoom_IdAndIdNot(
                            newRoomId,
                            tenantId
                    );

            // Kiểm tra phòng có hợp đồng hiệu lực của khách khác không
            boolean hasOtherActiveContract = tenantId == null
                    ? contractRepository.existsByRoom_IdAndStatus(
                            newRoomId,
                            ACTIVE_CONTRACT
                    )
                    : contractRepository
                            .existsByRoom_IdAndStatusAndTenant_IdNot(
                                    newRoomId,
                                    ACTIVE_CONTRACT,
                                    tenantId
                            );

            boolean roomIsRented =
                    ROOM_RENTED.equals(selectedRoom.getStatus());

            if (assignedToOtherTenant ||
                    hasOtherActiveContract ||
                    (roomIsRented && !sameRoom)) {

                throw new RuntimeException(
                        "Phòng " + selectedRoom.getRoomNumber() +
                        " đã có khách thuê! Vui lòng chọn phòng khác."
                );
            }

            // Gán phòng lấy từ database
            tenant.setRoom(selectedRoom);

        } else {

            /*
             * Không chọn phòng:
             * Gán null để tránh giữ đối tượng Room rỗng
             * được tạo ra từ form Thymeleaf.
             */
            tenant.setRoom(null);
        }

        // -------------------------------------------------
        // 10. CẬP NHẬT THÔNG TIN KHÁCH THUÊ
        // -------------------------------------------------

        tenant.setFullName(formTenant.getFullName());
        tenant.setPhone(formTenant.getPhone());
        tenant.setCccd(formTenant.getCccd());
        tenant.setEmail(formTenant.getEmail());

        // Lưu đối tượng Tenant đang được Hibernate quản lý
        return tenantRepository.save(tenant);
    }

    // -------------------------------------------------
    // XÓA KHÁCH THUÊ
    // -------------------------------------------------

    @Transactional
    public void deleteTenant(Long id) {

        Tenant tenant = getTenantById(id);

        // Không xóa khách thuê đã từng có hợp đồng
        boolean hasContract =
                contractRepository.existsByTenant_Id(id);

        if (hasContract) {

            throw new RuntimeException(
                    "Không thể xóa khách thuê này vì đang có hợp đồng liên quan! " +
                    "Hãy giữ lại hồ sơ để bảo toàn lịch sử thuê trọ."
            );
        }

        tenantRepository.delete(tenant);
    }
}
