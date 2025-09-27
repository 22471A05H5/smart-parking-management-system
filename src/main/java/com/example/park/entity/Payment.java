package com.example.park.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;
    
    @NotNull(message = "Amount is required")
    private BigDecimal amount;
    
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;
    
    @Enumerated(EnumType.STRING)
    private PaymentStatus status = PaymentStatus.PENDING;
    
    private String transactionId;
    
    private String paymentGatewayResponse;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    public enum PaymentMethod {
        CREDIT_CARD, DEBIT_CARD, UPI, NET_BANKING, WALLET, CASH
    }
    
    public enum PaymentStatus {
        PENDING, PROCESSING, COMPLETED, FAILED, REFUNDED
    }
}
