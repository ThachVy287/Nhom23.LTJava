package com.example.demo.service;

import com.example.demo.model.Invoice;
import com.example.demo.model.Room;
import com.example.demo.repository.InvoiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
        if (invoice.getRoom() != null && invoice.getRoom().getId() != null) {
            Room room = roomService.getRoomById(invoice.getRoom().getId());
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