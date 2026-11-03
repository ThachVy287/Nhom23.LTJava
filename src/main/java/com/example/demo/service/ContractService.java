
package com.example.demo.service;

import com.example.demo.model.Contract;
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
public class ContractService {

    private static final String ACTIVE = "Đang hiệu lực";
    private static final String TERMINATED = "Đã thanh lý";

    private static final String ROOM_AVAILABLE = "TRONG";
    private static final String ROOM_RENTED = "DA_THUE";

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private TenantRepository tenantRepository;

    // Lấy danh sách hợp đồng
    @Transactional(readOnly = true)
    public List<Contract> getAllContracts() {
        return contractRepository.findAll();
    }

    // Tìm hợp đồng theo ID
    @Transactional(readOnly = true)
    public Contract getContractById(Long id) {

        if (id == null) {
            throw new RuntimeException(
                    "ID hợp đồng không hợp lệ!"
            );
        }

        return contractRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy hợp đồng ID: " + id
                        ));
    }

    // Thêm mới hoặc cập nhật hợp đồng
    @Transactional
    public Contract saveContract(Contract formContract) {

        if (formContract == null) {
            throw new RuntimeException(
                    "Thông tin hợp đồng không hợp lệ!"
            );
        }

        Long contractId = formContract.getId();

        Contract contract;
        Long oldRoomId = null;
        Long oldTenantId = null;

        // -------------------------------------------------
        // 1. LẤY HỢP ĐỒNG HIỆN TẠI
        // -------------------------------------------------

        if (contractId != null) {

            contract = getContractById(contractId);

            if (contract.getRoom() != null) {
                oldRoomId = contract.getRoom().getId();
            }

            if (contract.getTenant() != null) {
                oldTenantId = contract.getTenant().getId();
            }

        } else {

            // Hợp đồng mới mặc định đang hiệu lực
            contract = new Contract();
            contract.setStatus(ACTIVE);
        }

        // -------------------------------------------------
        // 2. KIỂM TRA NGÀY THÁNG
        // -------------------------------------------------

        if (formContract.getStartDate() == null ||
                formContract.getEndDate() == null) {

            throw new RuntimeException(
                    "Vui lòng nhập đầy đủ ngày bắt đầu và ngày kết thúc!"
            );
        }

        if (formContract.getEndDate()
                .isBefore(formContract.getStartDate())) {

            throw new RuntimeException(
                    "Ngày kết thúc không được trước ngày bắt đầu!"
            );
        }

        // -------------------------------------------------
        // 3. KIỂM TRA TIỀN CỌC
        // -------------------------------------------------

        if (formContract.getDeposit() == null ||
                !Double.isFinite(formContract.getDeposit()) ||
                formContract.getDeposit() < 0) {

            throw new RuntimeException(
                    "Tiền cọc phải là số hợp lệ và không được âm!"
            );
        }

        // -------------------------------------------------
        // 4. KIỂM TRA GIÁ THUÊ
        // -------------------------------------------------

        if (formContract.getMonthlyRent() == null ||
                !Double.isFinite(formContract.getMonthlyRent()) ||
                formContract.getMonthlyRent() <= 0) {

            throw new RuntimeException(
                    "Giá thuê hàng tháng phải lớn hơn 0!"
            );
        }

        // -------------------------------------------------
        // 5. XỬ LÝ RIÊNG HỢP ĐỒNG ĐÃ THANH LÝ
        // -------------------------------------------------

        if (TERMINATED.equals(contract.getStatus())) {

            /*
             * Hợp đồng đã thanh lý:
             *
             * - Giữ nguyên phòng cũ.
             * - Giữ nguyên khách thuê cũ.
             * - Giữ nguyên trạng thái đã thanh lý.
             * - Chỉ cập nhật thông tin thời hạn và tài chính.
             *
             * Không lấy Room hoặc Tenant từ form.
             * Không gán lại đối tượng liên kết.
             * Không đồng bộ lại trạng thái phòng.
             */

            contract.setStartDate(formContract.getStartDate());
            contract.setEndDate(formContract.getEndDate());
            contract.setDeposit(formContract.getDeposit());
            contract.setMonthlyRent(formContract.getMonthlyRent());

            return contractRepository.save(contract);
        }

        // -------------------------------------------------
        // 6. KIỂM TRA PHÒNG VÀ KHÁCH THUÊ
        // -------------------------------------------------

        if (formContract.getRoom() == null ||
                formContract.getRoom().getId() == null) {

            throw new RuntimeException(
                    "Vui lòng chọn phòng trọ!"
            );
        }

        if (formContract.getTenant() == null ||
                formContract.getTenant().getId() == null) {

            throw new RuntimeException(
                    "Vui lòng chọn khách thuê!"
            );
        }

        Long newRoomId = formContract.getRoom().getId();
        Long newTenantId = formContract.getTenant().getId();

        // Lấy phòng thực tế và đang được quản lý bởi Hibernate
        Room newRoom = roomRepository.findById(newRoomId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Phòng trọ không tồn tại!"
                        ));

        // Lấy khách thuê thực tế từ database
        Tenant newTenant = tenantRepository.findById(newTenantId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Khách thuê không tồn tại!"
                        ));

        // -------------------------------------------------
        // 7. KIỂM TRA TRẠNG THÁI HỢP ĐỒNG
        // -------------------------------------------------

        if (contract.getStatus() == null ||
                contract.getStatus().isBlank()) {

            contract.setStatus(ACTIVE);
        }

        String status = contract.getStatus();

        if (!ACTIVE.equals(status) &&
                !TERMINATED.equals(status)) {

            throw new RuntimeException(
                    "Trạng thái hợp đồng không hợp lệ!"
            );
        }

        // -------------------------------------------------
        // 8. KIỂM TRA KHÔNG CHO THUÊ TRÙNG PHÒNG
        // -------------------------------------------------

        if (ACTIVE.equals(status)) {

            boolean sameCurrentRoom =
                    contractId != null &&
                    oldRoomId != null &&
                    oldRoomId.equals(newRoomId);

            // Kiểm tra phòng đã có hợp đồng hiệu lực khác
            boolean hasOtherActiveContract;

            if (contractId == null) {

                hasOtherActiveContract =
                        contractRepository.existsByRoom_IdAndStatus(
                                newRoomId,
                                ACTIVE
                        );

            } else {

                hasOtherActiveContract =
                        contractRepository
                                .existsByRoom_IdAndStatusAndIdNot(
                                        newRoomId,
                                        ACTIVE,
                                        contractId
                                );
            }

            if (hasOtherActiveContract) {

                throw new RuntimeException(
                        "Phòng " + newRoom.getRoomNumber() +
                        " đã có hợp đồng đang hiệu lực!"
                );
            }

            // Kiểm tra phòng đã được gán cho khách thuê khác
            boolean assignedToOtherTenant =
                    tenantRepository.existsByRoom_IdAndIdNot(
                            newRoomId,
                            newTenantId
                    );

            if (assignedToOtherTenant) {

                throw new RuntimeException(
                        "Phòng " + newRoom.getRoomNumber() +
                        " đã được gán cho khách thuê khác!"
                );
            }

            // Kiểm tra trạng thái phòng
            if (ROOM_RENTED.equals(newRoom.getStatus()) &&
                    !sameCurrentRoom) {

                throw new RuntimeException(
                        "Phòng " + newRoom.getRoomNumber() +
                        " hiện đang được thuê! Không thể lập hợp đồng mới."
                );
            }

            // Một khách thuê không được có hai hợp đồng hiệu lực
            boolean tenantHasOtherActiveContract;

            if (contractId == null) {

                tenantHasOtherActiveContract =
                        contractRepository.existsByTenant_IdAndStatus(
                                newTenantId,
                                ACTIVE
                        );

            } else {

                tenantHasOtherActiveContract =
                        contractRepository
                                .existsByTenant_IdAndStatusAndIdNot(
                                        newTenantId,
                                        ACTIVE,
                                        contractId
                                );
            }

            if (tenantHasOtherActiveContract) {

                throw new RuntimeException(
                        "Khách thuê này đã có hợp đồng đang hiệu lực!"
                );
            }
        }

        // -------------------------------------------------
        // 9. GÁN THÔNG TIN VÀ LƯU HỢP ĐỒNG
        // -------------------------------------------------

        contract.setRoom(newRoom);
        contract.setTenant(newTenant);
        contract.setStartDate(formContract.getStartDate());
        contract.setEndDate(formContract.getEndDate());
        contract.setDeposit(formContract.getDeposit());
        contract.setMonthlyRent(formContract.getMonthlyRent());

        Contract savedContract = contractRepository.save(contract);

        // -------------------------------------------------
        // 10. ĐỒNG BỘ PHÒNG VÀ KHÁCH THUÊ
        // -------------------------------------------------

        if (ACTIVE.equals(status)) {

            // Cập nhật phòng hiện tại cho khách thuê
            newTenant.setRoom(newRoom);
            tenantRepository.save(newTenant);

            // Nếu đổi phòng, cập nhật lại phòng cũ
            if (oldRoomId != null &&
                    !oldRoomId.equals(newRoomId)) {

                updateRoomStatus(oldRoomId);
            }

            // Đánh dấu phòng mới đã thuê
            updateRoomStatus(newRoomId);
        }

        // Nếu thay khách thuê trong hợp đồng
        if (oldTenantId != null &&
                !oldTenantId.equals(newTenantId) &&
                ACTIVE.equals(status)) {

            Tenant oldTenant = tenantRepository.findById(oldTenantId)
                    .orElse(null);

            if (oldTenant != null &&
                    oldTenant.getRoom() != null &&
                    oldRoomId != null &&
                    oldTenant.getRoom().getId().equals(oldRoomId)) {

                boolean oldTenantHasActiveContract =
                        contractRepository.existsByTenant_IdAndStatus(
                                oldTenantId,
                                ACTIVE
                        );

                if (!oldTenantHasActiveContract) {

                    oldTenant.setRoom(null);
                    tenantRepository.save(oldTenant);
                }
            }
        }

        return savedContract;
    }

    // -------------------------------------------------
    // THANH LÝ HỢP ĐỒNG
    // -------------------------------------------------

    @Transactional
    public void terminateContract(Long id) {

        Contract contract = getContractById(id);

        if (TERMINATED.equals(contract.getStatus())) {

            throw new RuntimeException(
                    "Hợp đồng này đã được thanh lý trước đó!"
            );
        }

        Long roomId = contract.getRoom() != null
                ? contract.getRoom().getId()
                : null;

        Long tenantId = contract.getTenant() != null
                ? contract.getTenant().getId()
                : null;

        contract.setStatus(TERMINATED);
        contractRepository.save(contract);

        // Kiểm tra khách thuê còn hợp đồng hiệu lực khác không
        if (tenantId != null) {

            Tenant tenant = tenantRepository.findById(tenantId)
                    .orElse(null);

            boolean hasOtherActiveContract =
                    contractRepository.existsByTenant_IdAndStatus(
                            tenantId,
                            ACTIVE
                    );

            // Nếu không còn hợp đồng hiệu lực thì bỏ gán phòng hiện tại
            if (tenant != null &&
                    !hasOtherActiveContract &&
                    tenant.getRoom() != null &&
                    roomId != null &&
                    tenant.getRoom().getId().equals(roomId)) {

                tenant.setRoom(null);
                tenantRepository.save(tenant);
            }
        }

        // Cập nhật lại trạng thái phòng
        if (roomId != null) {
            updateRoomStatus(roomId);
        }
    }

    // -------------------------------------------------
    // ĐỒNG BỘ TRẠNG THÁI PHÒNG
    // -------------------------------------------------

    private void updateRoomStatus(Long roomId) {

        if (roomId == null) {
            return;
        }

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy phòng ID: " + roomId
                        ));

        boolean hasActiveContract =
                contractRepository.existsByRoom_IdAndStatus(
                        roomId,
                        ACTIVE
                );

        room.setStatus(
                hasActiveContract
                        ? ROOM_RENTED
                        : ROOM_AVAILABLE
        );

        roomRepository.save(room);
    }

    // -------------------------------------------------
    // XÓA HỢP ĐỒNG
    // -------------------------------------------------

    // Giữ phương thức để tương thích với Controller cũ.
    // Không xóa hợp đồng trực tiếp để bảo toàn lịch sử thuê.
    @Transactional
    public void deleteContract(Long id) {

        getContractById(id);

        throw new RuntimeException(
                "Không xóa trực tiếp hợp đồng. " +
                "Vui lòng sử dụng chức năng Thanh lý hợp đồng."
        );
    }
}
