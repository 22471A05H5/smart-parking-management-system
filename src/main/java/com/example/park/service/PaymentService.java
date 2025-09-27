package com.example.park.service;

import com.example.park.entity.Booking;
import com.example.park.entity.Payment;
import com.example.park.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final BookingService bookingService;
    
    public Payment createPayment(Payment payment) {
        payment.setStatus(Payment.PaymentStatus.PENDING);
        payment.setTransactionId(generateTransactionId());
        return paymentRepository.save(payment);
    }
    
    public Payment processPayment(Long paymentId, Payment.PaymentMethod paymentMethod) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        
        payment.setPaymentMethod(paymentMethod);
        payment.setStatus(Payment.PaymentStatus.PROCESSING);
        
        // Simulate payment processing
        boolean paymentSuccess = simulatePaymentGateway(payment);
        
        if (paymentSuccess) {
            payment.setStatus(Payment.PaymentStatus.COMPLETED);
            payment.setPaymentGatewayResponse("Payment successful");
            
            // Confirm the booking after successful payment
            bookingService.confirmBooking(payment.getBooking().getId());
        } else {
            payment.setStatus(Payment.PaymentStatus.FAILED);
            payment.setPaymentGatewayResponse("Payment failed - insufficient funds");
        }
        
        return paymentRepository.save(payment);
    }
    
    public Payment refundPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        
        if (payment.getStatus() != Payment.PaymentStatus.COMPLETED) {
            throw new RuntimeException("Only completed payments can be refunded");
        }
        
        payment.setStatus(Payment.PaymentStatus.REFUNDED);
        payment.setPaymentGatewayResponse("Payment refunded successfully");
        
        return paymentRepository.save(payment);
    }
    
    @Transactional(readOnly = true)
    public Optional<Payment> findById(Long id) {
        return paymentRepository.findById(id);
    }
    
    @Transactional(readOnly = true)
    public Optional<Payment> findByBooking(Booking booking) {
        return paymentRepository.findByBooking(booking);
    }
    
    @Transactional(readOnly = true)
    public Optional<Payment> findByTransactionId(String transactionId) {
        return paymentRepository.findByTransactionId(transactionId);
    }
    
    @Transactional(readOnly = true)
    public List<Payment> findByStatus(Payment.PaymentStatus status) {
        return paymentRepository.findByStatus(status);
    }
    
    @Transactional(readOnly = true)
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public Double getTodayRevenue() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        Double revenue = paymentRepository.getTotalPaymentsBetweenDates(startOfDay, endOfDay);
        return revenue != null ? revenue : 0.0;
    }
    
    @Transactional(readOnly = true)
    public long getFailedPaymentsCount() {
        return paymentRepository.countFailedPayments();
    }
    
    private String generateTransactionId() {
        return "TXN_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }
    
    private boolean simulatePaymentGateway(Payment payment) {
        // Simulate payment gateway processing
        // In real implementation, this would integrate with actual payment gateways
        // For demo purposes, we'll simulate 90% success rate
        return Math.random() > 0.1;
    }
}
