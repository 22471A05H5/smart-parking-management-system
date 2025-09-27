package com.example.park.repository;

import com.example.park.entity.Payment;
import com.example.park.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    Optional<Payment> findByBooking(Booking booking);
    
    List<Payment> findByStatus(Payment.PaymentStatus status);
    
    List<Payment> findByPaymentMethod(Payment.PaymentMethod paymentMethod);
    
    Optional<Payment> findByTransactionId(String transactionId);
    
    @Query("SELECT p FROM Payment p WHERE p.status = 'COMPLETED' AND " +
           "p.createdAt >= :startDate AND p.createdAt <= :endDate")
    List<Payment> findCompletedPaymentsBetweenDates(@Param("startDate") LocalDateTime startDate,
                                                   @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'COMPLETED' AND " +
           "p.createdAt >= :startDate AND p.createdAt <= :endDate")
    Double getTotalPaymentsBetweenDates(@Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = 'FAILED'")
    long countFailedPayments();
}
