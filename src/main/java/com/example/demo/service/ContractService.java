package com.example.demo.service;

import com.example.demo.model.Contract;
import com.example.demo.model.Room;
import com.example.demo.repository.ContractRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContractService {

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private RoomService roomService;

    public List<Contract> getAllContracts() {
        return contractRepository.findAll();
    }

    public Contract getContractById(Long id) {
        return contractRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hợp đồng ID: " + id));
    }

    public Contract saveContract(Contract contract) {
        // Khi tạo hợp đồng mới, tự động cập nhật trạng thái phòng thành "Đã thuê"
        if (contract.getRoom() != null && contract.getRoom().getId() != null) {
            Room room = roomService.getRoomById(contract.getRoom().getId());
            room.setStatus("DA_THUE");
            roomService.saveRoom(room);
        }
        return contractRepository.save(contract);
    }

    public void deleteContract(Long id) {
        Contract contract = getContractById(id);
        // Khi xóa/thanh lý hợp đồng, trả phòng về trạng thái "Còn trống"
        if (contract.getRoom() != null) {
            Room room = contract.getRoom();
            room.setStatus("TRONG");
            roomService.saveRoom(room);
        }
        contractRepository.deleteById(id);
    }
}