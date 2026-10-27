package com.example.demo.service;

import com.example.demo.model.Contract;
import com.example.demo.model.Room;
import com.example.demo.repository.ContractRepository;
import com.example.demo.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContractService {

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private RoomService roomService;

    @Autowired
    private RoomRepository roomRepository; // Thêm repository của Room để lưu trực tiếp trạng thái phòng

    public List<Contract> getAllContracts() {
        return contractRepository.findAll();
    }

    public Contract getContractById(Long id) {
        return contractRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hợp đồng ID: " + id));
    }

    public Contract saveContract(Contract contract) {
        // Khi tạo hợp đồng mới, tự động cập nhật trạng thái phòng thành "DA_THUE"
        if (contract.getRoom() != null && contract.getRoom().getId() != null) {
            Room room = roomService.getRoomById(contract.getRoom().getId());
            room.setStatus("DA_THUE");
            roomRepository.save(room); // Lưu trực tiếp qua roomRepository để không ảnh hưởng phần upload ảnh của phòng
        }
        return contractRepository.save(contract);
    }

    public void deleteContract(Long id) {
        Contract contract = getContractById(id);
        // Khi xóa/thanh lý hợp đồng, trả phòng về trạng thái "TRONG"
        if (contract.getRoom() != null) {
            Room room = contract.getRoom();
            room.setStatus("TRONG");
            roomRepository.save(room); // Lưu trực tiếp qua roomRepository
        }
        contractRepository.deleteById(id);
    }
}