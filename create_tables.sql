-- Create database tables for Smart Parking Management System
USE transport_db;

-- Drop existing tables if they exist
DROP TABLE IF EXISTS bookings;
DROP TABLE IF EXISTS parking_slots;
DROP TABLE IF EXISTS users;

-- Create users table
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'DRIVER') NOT NULL,
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create parking_slots table
CREATE TABLE parking_slots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    slot_number VARCHAR(20) NOT NULL UNIQUE,
    zone VARCHAR(50) NOT NULL,
    hourly_rate DECIMAL(10,2) NOT NULL,
    status ENUM('AVAILABLE', 'OCCUPIED', 'RESERVED', 'MAINTENANCE') DEFAULT 'AVAILABLE',
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create bookings table
CREATE TABLE bookings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    parking_slot_id BIGINT NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    vehicle_number VARCHAR(20) NOT NULL,
    booking_status ENUM('ACTIVE', 'COMPLETED', 'CANCELLED') DEFAULT 'ACTIVE',
    payment_status ENUM('PENDING', 'PAID', 'REFUNDED') DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (parking_slot_id) REFERENCES parking_slots(id) ON DELETE CASCADE
);

-- Insert default admin user
INSERT INTO users (username, password, role, enabled) 
VALUES ('admin', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'ADMIN', TRUE);

-- Insert default parking slots
INSERT INTO parking_slots (slot_number, zone, hourly_rate, status, enabled) VALUES
('A001', 'Zone A', 50.00, 'AVAILABLE', TRUE),
('A002', 'Zone A', 50.00, 'AVAILABLE', TRUE),
('A003', 'Zone A', 50.00, 'AVAILABLE', TRUE),
('A004', 'Zone A', 50.00, 'AVAILABLE', TRUE),
('A005', 'Zone A', 50.00, 'AVAILABLE', TRUE),
('B001', 'Zone B', 60.00, 'AVAILABLE', TRUE),
('B002', 'Zone B', 60.00, 'AVAILABLE', TRUE),
('B003', 'Zone B', 60.00, 'AVAILABLE', TRUE),
('B004', 'Zone B', 60.00, 'AVAILABLE', TRUE),
('B005', 'Zone B', 60.00, 'AVAILABLE', TRUE),
('C001', 'Zone C', 70.00, 'AVAILABLE', TRUE),
('C002', 'Zone C', 70.00, 'AVAILABLE', TRUE),
('C003', 'Zone C', 70.00, 'AVAILABLE', TRUE),
('C004', 'Zone C', 70.00, 'AVAILABLE', TRUE),
('C005', 'Zone C', 70.00, 'AVAILABLE', TRUE),
('D001', 'Zone D', 80.00, 'AVAILABLE', TRUE),
('D002', 'Zone D', 80.00, 'AVAILABLE', TRUE),
('D003', 'Zone D', 80.00, 'AVAILABLE', TRUE),
('D004', 'Zone D', 80.00, 'AVAILABLE', TRUE),
('D005', 'Zone D', 80.00, 'AVAILABLE', TRUE);

SELECT 'Database tables created successfully!' as status;
