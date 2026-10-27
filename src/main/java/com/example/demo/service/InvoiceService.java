package com.example.demo.service;

import com.example.demo.model.Invoice;
import com.example.demo.model.Room;
import com.example.demo.repository.InvoiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class InvoiceService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private RoomService roomService;

    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }

    public Invoice getInvoiceById(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn ID: " + id));
    }

    public Invoice saveInvoice(Invoice invoice) {
        Long roomId = invoice.getRoom() != null ? invoice.getRoom().getId() : null;
        
        if (roomId != null) {
            // Kiểm tra ràng buộc với tháng trước đó
            Optional<Invoice> lastInvoiceOpt = invoiceRepository.findTopByRoomIdOrderByIdDesc(roomId);
            
            if (lastInvoiceOpt.isPresent()) {
                Invoice lastInvoice = lastInvoiceOpt.get();
                
                if (invoice.getId() == null || !invoice.getId().equals(lastInvoice.getId())) {
                    // Số điện cũ phải bằng số điện mới của tháng trước
                    if (invoice.getOldElectricity() != null && !invoice.getOldElectricity().equals(lastInvoice.getNewElectricity())) {
                        throw new RuntimeException("Số điện cũ phải bằng số điện mới của tháng trước (" + lastInvoice.getNewElectricity() + ")");
                    }
                    
                    // Số nước cũ phải bằng số nước mới của tháng trước
                    if (invoice.getOldWater() != null && !invoice.getOldWater().equals(lastInvoice.getNewWater())) {
                        throw new RuntimeException("Số nước cũ phải bằng số nước mới của tháng trước (" + lastInvoice.getNewWater() + ")");
                    }
                }
            }
        }

        // Số mới không được nhỏ hơn số cũ
        if (invoice.getNewElectricity() != null && invoice.getOldElectricity() != null) {
            if (invoice.getNewElectricity() < invoice.getOldElectricity()) {
                throw new RuntimeException("Số điện mới không được thấp hơn số điện cũ!");
            }
        }

        if (invoice.getNewWater() != null && invoice.getOldWater() != null) {
            if (invoice.getNewWater() < invoice.getOldWater()) {
                throw new RuntimeException("Số nước mới không được thấp hơn số nước cũ!");
            }
        }

        // Tính tiền điện tiêu thụ
        double elecUsed = (invoice.getNewElectricity() != null && invoice.getOldElectricity() != null)
                ? (invoice.getNewElectricity() - invoice.getOldElectricity()) : 0;
        double elecTotal = elecUsed * (invoice.getElectricityPrice() != null ? invoice.getElectricityPrice() : 0);

        // Tính tiền nước tiêu thụ
        double waterUsed = (invoice.getNewWater() != null && invoice.getOldWater() != null)
                ? (invoice.getNewWater() - invoice.getOldWater()) : 0;
        double waterTotal = waterUsed * (invoice.getWaterPrice() != null ? invoice.getWaterPrice() : 0);

        // Lấy giá phòng cơ bản
        double roomPrice = 0;
        if (roomId != null) {
            Room room = roomService.getRoomById(roomId);
            roomPrice = room.getPrice() != null ? room.getPrice() : 0;
        }

        // Tổng tiền = Tiền phòng + Tiền điện + Tiền nước
        invoice.setTotalAmount(roomPrice + elecTotal + waterTotal);
        
        if (invoice.getStatus() == null) {
            invoice.setStatus("CHUA_THANH_TOAN");
        }

        return invoiceRepository.save(invoice);
    }

    public void deleteInvoice(Long id) {
        invoiceRepository.deleteById(id);
    }
}