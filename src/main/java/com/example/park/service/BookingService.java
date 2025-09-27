package com.example.park.service;

import com.example.park.entity.Booking;
import com.example.park.entity.Slot;
import com.example.park.entity.User;
import com.example.park.repository.BookingRepository;
import com.example.park.service.SlotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BookingService {
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private SlotService slotService;
    
    public Booking createBooking(User user, Long slotId, LocalDateTime startTime, LocalDateTime endTime, String vehicleNumber) {
        // Get slot
        Slot slot = slotService.getSlotById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot not found"));
        
        // Validate slot availability
        List<Booking> conflictingBookings = bookingRepository.findConflictingBookings(
                slot, startTime, endTime);
        
        if (!conflictingBookings.isEmpty()) {
            throw new RuntimeException("Parking slot is not available for the selected time period");
        }
        
        // Create booking
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setSlot(slot);
        booking.setStartTime(startTime);
        booking.setEndTime(endTime);
        booking.setVehicleNumber(vehicleNumber);
        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        booking.setPaymentStatus(Booking.PaymentStatus.PENDING);
        
        // Calculate total amount
        BigDecimal totalAmount = calculateBookingAmount(booking);
        booking.setTotalAmount(totalAmount);
        
        // Update slot status to reserved/occupied
        if (startTime.isAfter(LocalDateTime.now())) {
            slotService.updateSlotStatus(slotId, Slot.SlotStatus.RESERVED);
        } else {
            slotService.updateSlotStatus(slotId, Slot.SlotStatus.OCCUPIED);
            booking.setStatus(Booking.BookingStatus.ACTIVE);
        }
        
        return bookingRepository.save(booking);
    }
    
    public Booking confirmBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        
        return bookingRepository.save(booking);
    }
    
    public Booking activateBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        booking.setStatus(Booking.BookingStatus.ACTIVE);
        slotService.updateSlotStatus(booking.getSlot().getId(), Slot.SlotStatus.OCCUPIED);
        
        return bookingRepository.save(booking);
    }
    
    public Booking completeBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        booking.setStatus(Booking.BookingStatus.COMPLETED);
        slotService.updateSlotStatus(booking.getSlot().getId(), Slot.SlotStatus.AVAILABLE);
        
        return bookingRepository.save(booking);
    }
    
    public Booking cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        // Check if booking can be canceled (within 5 minutes of creation)
        if (!canCancelBooking(booking)) {
            throw new RuntimeException("Booking can only be canceled within 5 minutes of creation");
        }
        
        booking.setStatus(Booking.BookingStatus.CANCELLED);
        // Release the slot back to available status
        slotService.updateSlotStatus(booking.getSlot().getId(), Slot.SlotStatus.AVAILABLE);
        
        return bookingRepository.save(booking);
    }
    
    public boolean canCancelBooking(Booking booking) {
        if (booking.getStatus() == Booking.BookingStatus.CANCELLED || 
            booking.getStatus() == Booking.BookingStatus.COMPLETED) {
            return false;
        }
        
        LocalDateTime createdAt = booking.getCreatedAt();
        LocalDateTime now = LocalDateTime.now();
        Duration timeSinceCreation = Duration.between(createdAt, now);
        
        // Allow cancellation within 5 minutes (300 seconds)
        return timeSinceCreation.toSeconds() <= 300;
    }
    
    public long getCancellationTimeRemaining(Booking booking) {
        if (!canCancelBooking(booking)) {
            return 0;
        }
        
        LocalDateTime createdAt = booking.getCreatedAt();
        LocalDateTime now = LocalDateTime.now();
        Duration timeSinceCreation = Duration.between(createdAt, now);
        
        // Return remaining seconds (300 - elapsed seconds)
        long remainingSeconds = 300 - timeSinceCreation.toSeconds();
        return Math.max(0, remainingSeconds);
    }
    
    @Transactional(readOnly = true)
    public Optional<Booking> findById(Long id) {
        return bookingRepository.findById(id);
    }
    
    @Transactional(readOnly = true)
    public List<Booking> findByUser(User user) {
        return bookingRepository.findByUserOrderByCreatedAtDesc(user);
    }
    
    public void updatePaymentStatus(Long bookingId, Booking.PaymentStatus status) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isPresent()) {
            Booking booking = bookingOpt.get();
            booking.setPaymentStatus(status);
            bookingRepository.save(booking);
        }
    }
    
    @Transactional(readOnly = true)
    public List<Booking> findByStatus(Booking.BookingStatus status) {
        return bookingRepository.findByStatus(status);
    }
    
    @Transactional(readOnly = true)
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }
    
    public void processExpiredBookings() {
        List<Booking> expiredBookings = bookingRepository.findExpiredActiveBookings(LocalDateTime.now());
        for (Booking booking : expiredBookings) {
            completeBooking(booking.getId());
        }
    }
    
    @Transactional(readOnly = true)
    public long getActiveBookingsCount() {
        return bookingRepository.countActiveBookings();
    }
    
    @Transactional(readOnly = true)
    public long getTodayBookingsCount() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        return bookingRepository.countBookingsBetweenDates(startOfDay, endOfDay);
    }
    
    @Transactional(readOnly = true)
    public Double getTodayRevenue() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        Double revenue = bookingRepository.getTotalRevenueBetweenDates(startOfDay, endOfDay);
        return revenue != null ? revenue : 0.0;
    }
    
    private BigDecimal calculateBookingAmount(Booking booking) {
        Duration duration = Duration.between(booking.getStartTime(), booking.getEndTime());
        long hours = duration.toHours();
        if (duration.toMinutes() % 60 > 0) {
            hours++; // Round up for partial hours
        }
        
        BigDecimal hourlyRate = booking.getSlot().getPricePerHour();
        BigDecimal calculatedAmount = hourlyRate.multiply(BigDecimal.valueOf(hours));
        
        // Ensure minimum amount for payment processing (₹50 minimum)
        BigDecimal minimumAmount = new BigDecimal("50.00");
        return calculatedAmount.compareTo(minimumAmount) < 0 ? minimumAmount : calculatedAmount;
    }
}
