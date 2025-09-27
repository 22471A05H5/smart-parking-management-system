package com.example.park.controller;

import com.example.park.entity.Booking;
import com.example.park.entity.Slot;
import com.example.park.entity.Payment;
import com.example.park.entity.User;
import com.example.park.service.BookingService;
import com.example.park.service.SlotService;
import com.example.park.service.PaymentService;
import com.example.park.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    private final SlotService slotService;
    private final BookingService bookingService;
    private final PaymentService paymentService;
    
    @GetMapping("/dashboard")
    public String userDashboard(Principal principal, Model model) {
        User user = userService.findByUsername(principal.getName()).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }
        
        List<Booking> userBookings = bookingService.findByUser(user);
        List<Slot> availableSlots = slotService.getAllAvailableSlots();
        
        model.addAttribute("user", user);
        model.addAttribute("bookings", userBookings);
        model.addAttribute("availableSlots", availableSlots.size());
        
        return "user/dashboard";
    }
    
    @GetMapping("/book-slot")
    public String bookSlotPage(Model model) {
        List<Slot> availableSlots = slotService.getAllAvailableSlots();
        model.addAttribute("availableSlots", availableSlots);
        model.addAttribute("booking", new Booking());
        return "user/book-slot";
    }
    
    @PostMapping("/book-slot")
    public String bookSlot(@Valid @ModelAttribute("booking") Booking booking,
                          BindingResult bindingResult,
                          Principal principal,
                          RedirectAttributes redirectAttributes,
                          Model model) {
        
        if (bindingResult.hasErrors()) {
            List<Slot> availableSlots = slotService.getAllAvailableSlots();
            model.addAttribute("availableSlots", availableSlots);
            return "user/book-slot";
        }
        
        try {
            User user = userService.findByUsername(principal.getName()).orElse(null);
            
            // Validate booking times
            if (booking.getStartTime().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("Start time cannot be in the past");
            }
            if (booking.getEndTime().isBefore(booking.getStartTime())) {
                throw new RuntimeException("End time must be after start time");
            }
            
            // Use the new createBooking method signature
            Booking savedBooking = bookingService.createBooking(
                user, 
                booking.getSlot().getId(), 
                booking.getStartTime(), 
                booking.getEndTime(), 
                booking.getVehicleNumber()
            );
            
            // Create payment for the booking
            Payment payment = new Payment();
            payment.setBooking(savedBooking);
            payment.setAmount(savedBooking.getTotalAmount());
            paymentService.createPayment(payment);
            
            redirectAttributes.addFlashAttribute("success", "Booking created successfully! Please proceed with payment.");
            return "redirect:/user/booking/" + savedBooking.getId();
            
        } catch (RuntimeException e) {
            List<Slot> availableSlots = slotService.getAllAvailableSlots();
            model.addAttribute("availableSlots", availableSlots);
            model.addAttribute("error", e.getMessage());
            return "user/book-slot";
        }
    }
    
    @GetMapping("/booking/{id}")
    public String viewBooking(@PathVariable Long id, Principal principal, Model model) {
        Booking booking = bookingService.findById(id).orElse(null);
        if (booking == null || !booking.getUser().getUsername().equals(principal.getName())) {
            return "redirect:/user/dashboard";
        }
        
        Payment payment = paymentService.findByBooking(booking).orElse(null);
        
        model.addAttribute("booking", booking);
        model.addAttribute("payment", payment);
        
        return "user/booking-details";
    }
    
    @PostMapping("/booking/{id}/pay")
    public String processPayment(@PathVariable Long id,
                                @RequestParam Payment.PaymentMethod paymentMethod,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {
        
        Booking booking = bookingService.findById(id).orElse(null);
        if (booking == null || !booking.getUser().getUsername().equals(principal.getName())) {
            return "redirect:/user/dashboard";
        }
        
        Payment payment = paymentService.findByBooking(booking).orElse(null);
        if (payment != null) {
            try {
                paymentService.processPayment(payment.getId(), paymentMethod);
                redirectAttributes.addFlashAttribute("success", "Payment processed successfully!");
            } catch (RuntimeException e) {
                redirectAttributes.addFlashAttribute("error", e.getMessage());
            }
        }
        
        return "redirect:/user/booking/" + id;
    }
    
    @PostMapping("/booking/{id}/cancel")
    public String cancelBooking(@PathVariable Long id,
                               Principal principal,
                               RedirectAttributes redirectAttributes) {
        
        Booking booking = bookingService.findById(id).orElse(null);
        if (booking == null || !booking.getUser().getUsername().equals(principal.getName())) {
            return "redirect:/user/dashboard";
        }
        
        try {
            bookingService.cancelBooking(id);
            redirectAttributes.addFlashAttribute("success", "Booking cancelled successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/user/dashboard";
    }
    
    @GetMapping("/bookings")
    public String userBookings(Principal principal, Model model) {
        User user = userService.findByUsername(principal.getName()).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }
        
        List<Booking> bookings = bookingService.findByUser(user);
        model.addAttribute("bookings", bookings);
        
        return "user/bookings";
    }
}
