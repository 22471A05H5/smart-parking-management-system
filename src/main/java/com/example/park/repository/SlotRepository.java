package com.example.park.repository;

import com.example.park.entity.Slot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SlotRepository extends JpaRepository<Slot, Long> {
    
    List<Slot> findByEnabledTrue();
    
    List<Slot> findByZoneAndEnabledTrue(String zone);
    
    List<Slot> findByStatusAndEnabledTrue(Slot.SlotStatus status);
    
    List<Slot> findByZoneAndStatusAndEnabledTrue(String zone, Slot.SlotStatus status);
    
    Optional<Slot> findBySlotNumber(String slotNumber);
    
    @Query("SELECT COUNT(s) FROM Slot s WHERE s.status = :status AND s.enabled = true")
    long countByStatus(Slot.SlotStatus status);
    
    boolean existsBySlotNumber(String slotNumber);
    
    @Query("SELECT COUNT(s) FROM Slot s WHERE s.enabled = true")
    long countAllEnabled();
    
    @Query("SELECT s FROM Slot s WHERE s.enabled = true ORDER BY s.zone, s.slotNumber")
    List<Slot> findAllEnabledOrderByZoneAndSlotNumber();
}
