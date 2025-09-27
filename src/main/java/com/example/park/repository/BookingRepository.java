package com.example.park.repository;

import com.example.park.entity.Booking;
import com.example.park.entity.User;
import com.example.park.entity.Slot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    List<Booking> findByUser(User user);
    
    List<Booking> findBySlot(Slot slot);
    
    List<Booking> findByStatus(Booking.BookingStatus status);
    
    List<Booking> findByUserOrderByCreatedAtDesc(User user);
    
    @Query("SELECT b FROM Booking b WHERE b.slot = :slot AND " +
           "((b.startTime <= :endTime AND b.endTime >= :startTime)) AND " +
           "b.status IN ('CONFIRMED', 'ACTIVE')")
    List<Booking> findConflictingBookings(@Param("slot") Slot slot,
                                         @Param("startTime") LocalDateTime startTime,
                                         @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT b FROM Booking b WHERE b.status = 'ACTIVE' AND b.endTime < :currentTime")
    List<Booking> findExpiredActiveBookings(@Param("currentTime") LocalDateTime currentTime);
    
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.status = 'ACTIVE'")
    long countActiveBookings();
    
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.createdAt >= :startDate AND b.createdAt <= :endDate")
    long countBookingsBetweenDates(@Param("startDate") LocalDateTime startDate, 
                                  @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT SUM(b.totalAmount) FROM Booking b WHERE b.status = 'COMPLETED' AND " +
           "b.createdAt >= :startDate AND b.createdAt <= :endDate")
    Double getTotalRevenueBetweenDates(@Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate);
}
