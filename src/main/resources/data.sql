-- 1. Chèn dữ liệu mẫu Dịch vụ (services)
INSERT IGNORE INTO services (id, service_name, price, unit) VALUES 
(1, 'Tiền Điện', 3500, 'kWh'),
(2, 'Tiền Nước', 20000, 'm3'),
(3, 'Internet / Wifi', 100000, 'tháng');

-- 2. Chèn dữ liệu mẫu Phòng trọ (Tạo nhiều phòng để thấy phân trang và tìm kiếm)
INSERT IGNORE INTO rooms (id, room_number, price, status, description) VALUES 
(1, 'P101', 1500000, 'DA_THUE', 'Phòng tầng 1, ban công thoáng mát'),
(2, 'P102', 1800000, 'TRONG', 'Phòng tầng 1, có gác xép, máy lạnh'),
(3, 'P103', 1600000, 'TRONG', 'Phòng tầng 1, cửa sổ hướng Đông'),
(4, 'P104', 1700000, 'TRONG', 'Phòng tầng 1, đầy đủ tiện nghi'),
(5, 'P201', 2000000, 'TRONG', 'Phòng tầng 2, khép kín, cửa sổ lớn'),
(6, 'P202', 2100000, 'DA_THUE', 'Phòng tầng 2, view đẹp, ban công rộng'),
(7, 'P203', 1900000, 'TRONG', 'Phòng tầng 2, giá rẻ sinh viên'),
(8, 'P204', 2200000, 'TRONG', 'Phòng tầng 2, diện tích lớn'),
(9, 'P301', 2500000, 'DA_THUE', 'Phòng tầng 3, cao cấp, đầy đủ nội thất'),
(10, 'P302', 2300000, 'TRONG', 'Phòng tầng 3, yên tĩnh, thoáng mát'),
(11, 'P303', 2400000, 'TRONG', 'Phòng tầng 3, gần cầu thang bộ'),
(12, 'P304', 2600000, 'TRONG', 'Phòng tầng 3, VIP, máy lạnh đời mới');

-- 3. Chèn dữ liệu mẫu Khách thuê (tenants)
INSERT IGNORE INTO tenants (id, full_name, phone, cccd, email, room_id) VALUES 
(1, 'Nguyễn Văn An', '0912345678', '012345678901', 'an.nguyen@gmail.com', 1),
(2, 'Trần Thị Bích', '0987654321', '098765432109', 'bich.tran@gmail.com', 6),
(3, 'Lê Hoàng Long', '0905123456', '045678912345', 'long.le@gmail.com', 9);

-- 4. Chèn dữ liệu mẫu Hợp đồng (contracts)
INSERT IGNORE INTO contracts (id, room_id, tenant_id, start_date, end_date, deposit, monthly_rent, status) VALUES 
(1, 1, 1, '2026-01-01', '2026-12-31', 1500000, 1500000, 'Đang hiệu lực'),
(2, 6, 2, '2026-03-01', '2027-03-01', 2100000, 2100000, 'Đang hiệu lực'),
(3, 9, 3, '2026-05-01', '2027-05-01', 2500000, 2500000, 'Đang hiệu lực');

-- 5. Chèn dữ liệu mẫu Hóa đơn (invoices)
INSERT IGNORE INTO invoices (id, room_id, month_year, old_electricity, new_electricity, electricity_price, old_water, new_water, water_price, total_amount, status) VALUES 
(1, 1, '2026-09', 100, 150, 3500, 20, 28, 20000, 1835000, 'DA_THANH_TOAN'),
(2, 1, '2026-10', 150, 210, 3500, 28, 35, 20000, 1850000, 'CHUA_THANH_TOAN'),
(3, 6, '2026-10', 80, 130, 3500, 15, 22, 20000, 2555000, 'DA_THANH_TOAN'),
(4, 9, '2026-10', 50, 110, 3500, 10, 18, 20000, 2870000, 'DA_THANH_TOAN');