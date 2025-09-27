package com.example.park.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "slots")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Slot {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String slotNumber;
    
    @Column(nullable = false)
    private String zone;
    
    @Column(name = "price_per_hour", nullable = false)
    private BigDecimal pricePerHour;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SlotStatus status = SlotStatus.AVAILABLE;
    
    @Column(nullable = false)
    private Boolean enabled = true;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @OneToMany(mappedBy = "slot", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Booking> bookings;
    
    public enum SlotStatus {
        AVAILABLE,
        OCCUPIED,
        RESERVED,
        MAINTENANCE
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
