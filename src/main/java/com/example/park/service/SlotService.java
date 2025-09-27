package com.example.park.service;

import com.example.park.entity.Slot;
import com.example.park.repository.SlotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.annotation.PostConstruct;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SlotService {
    
    @Autowired
    private SlotRepository slotRepository;
    
    public List<Slot> getAllAvailableSlots() {
        return slotRepository.findAllEnabledOrderByZoneAndSlotNumber();
    }
    
    public List<Slot> getSlotsByZone(String zone) {
        return slotRepository.findByZoneAndEnabledTrue(zone);
    }
    
    public List<Slot> getAvailableSlots() {
        return slotRepository.findByStatusAndEnabledTrue(Slot.SlotStatus.AVAILABLE);
    }
    
    public Optional<Slot> getSlotById(Long id) {
        return slotRepository.findById(id);
    }
    
    public Optional<Slot> getSlotByNumber(String slotNumber) {
        return slotRepository.findBySlotNumber(slotNumber);
    }
    
    public Slot saveSlot(Slot slot) {
        return slotRepository.save(slot);
    }
    
    public Slot createSlot(String slotNumber, String zone, Double pricePerHour) {
        if (slotRepository.existsBySlotNumber(slotNumber)) {
            throw new RuntimeException("Slot number already exists");
        }
        
        Slot slot = new Slot();
        slot.setSlotNumber(slotNumber);
        slot.setZone(zone);
        slot.setPricePerHour(BigDecimal.valueOf(pricePerHour));
        slot.setStatus(Slot.SlotStatus.AVAILABLE);
        
        return slotRepository.save(slot);
    }
    
    public void updateSlotStatus(Long slotId, Slot.SlotStatus status) {
        Optional<Slot> slotOpt = slotRepository.findById(slotId);
        if (slotOpt.isPresent()) {
            Slot slot = slotOpt.get();
            slot.setStatus(status);
            slot.setUpdatedAt(LocalDateTime.now());
            slotRepository.save(slot);
        } else {
            throw new RuntimeException("Slot not found with ID: " + slotId);
        }
    }
    
    public long getTotalSlotsCount() {
        return slotRepository.countAllEnabled();
    }
    
    public long getAvailableSlotsCount() {
        return slotRepository.countByStatus(Slot.SlotStatus.AVAILABLE);
    }
    
    public long getOccupiedSlotsCount() {
        return slotRepository.countByStatus(Slot.SlotStatus.OCCUPIED);
    }
    
    public long getReservedSlotsCount() {
        return slotRepository.countByStatus(Slot.SlotStatus.RESERVED);
    }
    
    @PostConstruct
    public void initializeDefaultSlots() {
        try {
            // Check if slots already exist
            if (slotRepository.count() == 0) {
                // Create default slots for different zones
                String[] zones = {"Zone A", "Zone B", "Zone C", "Zone D"};
                double[] prices = {50.0, 60.0, 70.0, 80.0};
                
                for (int i = 0; i < zones.length; i++) {
                    for (int j = 1; j <= 10; j++) {
                        Slot slot = new Slot();
                        slot.setSlotNumber(zones[i].charAt(5) + String.format("%03d", j));
                        slot.setZone(zones[i]);
                        slot.setPricePerHour(BigDecimal.valueOf(prices[i]));
                        slot.setStatus(Slot.SlotStatus.AVAILABLE);
                        slot.setEnabled(true);
                        slotRepository.save(slot);
                    }
                }
                
                System.out.println("Default parking slots initialized successfully!");
            }
        } catch (Exception e) {
            System.err.println("Error initializing default slots: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public List<Slot> getAllSlots() {
        return slotRepository.findAllEnabledOrderByZoneAndSlotNumber();
    }
    
    public Optional<Slot> findById(Long id) {
        return slotRepository.findById(id);
    }
    
    public void updateSlot(Long id, String slotNumber, String zone, Double pricePerHour, String status) {
        Slot slot = slotRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Slot not found"));
        
        // Check if slot number is being changed and if it already exists
        if (!slot.getSlotNumber().equals(slotNumber) && slotRepository.existsBySlotNumber(slotNumber)) {
            throw new RuntimeException("Slot number already exists");
        }
        
        slot.setSlotNumber(slotNumber);
        slot.setZone(zone);
        slot.setPricePerHour(BigDecimal.valueOf(pricePerHour));
        slot.setStatus(Slot.SlotStatus.valueOf(status));
        slot.setUpdatedAt(LocalDateTime.now());
        
        slotRepository.save(slot);
    }
    
    public void deleteSlot(Long id) {
        Slot slot = slotRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Slot not found"));
        
        // Check if slot has active bookings
        if (slot.getStatus() == Slot.SlotStatus.RESERVED || slot.getStatus() == Slot.SlotStatus.OCCUPIED) {
            throw new RuntimeException("Cannot delete slot with active bookings");
        }
        
        slotRepository.delete(slot);
    }
}
