package com.example.park.controller;

import com.example.park.entity.User;
import com.example.park.entity.Slot;
import com.example.park.entity.Booking;
import com.example.park.service.UserService;
import com.example.park.service.SlotService;
import com.example.park.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/driver")
public class DriverController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private SlotService slotService;
    
    @Autowired
    private BookingService bookingService;
    
    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        User driver = userService.findByUsername(principal.getName()).orElse(null);
        if (driver == null || driver.getRole() != User.Role.DRIVER) {
            return "redirect:/login";
        }
        
        // Get driver statistics
        List<Booking> driverBookings = bookingService.findByUser(driver);
        long activeBookings = driverBookings.stream()
            .filter(booking -> booking.getStatus() == Booking.BookingStatus.ACTIVE)
            .count();
        long totalBookings = driverBookings.size();
        long completedBookings = driverBookings.stream()
            .filter(booking -> booking.getStatus() == Booking.BookingStatus.COMPLETED)
            .count();
        
        // Get slot statistics
        List<Slot> allSlots = slotService.getAllSlots();
        long availableSlots = allSlots.stream()
            .filter(slot -> slot.getStatus() == Slot.SlotStatus.AVAILABLE)
            .count();
        long occupiedSlots = allSlots.stream()
            .filter(slot -> slot.getStatus() == Slot.SlotStatus.OCCUPIED)
            .count();
        
        model.addAttribute("driver", driver);
        model.addAttribute("activeBookings", activeBookings);
        model.addAttribute("totalBookings", totalBookings);
        model.addAttribute("completedBookings", completedBookings);
        model.addAttribute("availableSlots", availableSlots);
        model.addAttribute("occupiedSlots", occupiedSlots);
        model.addAttribute("totalSlots", allSlots.size());
        
        return "driver/dashboard";
    }
    
    @GetMapping("/slots")
    public String viewSlots(Model model, Principal principal) {
        User driver = userService.findByUsername(principal.getName()).orElse(null);
        if (driver == null || driver.getRole() != User.Role.DRIVER) {
            return "redirect:/login";
        }
        
        List<Slot> slots = slotService.getAllSlots();
        model.addAttribute("slots", slots);
        model.addAttribute("driver", driver);
        
        return "driver/slots";
    }
    
    @GetMapping("/slots/refresh")
    @ResponseBody
    public List<Slot> refreshSlots() {
        return slotService.getAllSlots();
    }
    
    @GetMapping("/dashboard/refresh")
    @ResponseBody
    public java.util.Map<String, Object> refreshDashboard(Principal principal) {
        User driver = userService.findByUsername(principal.getName()).orElse(null);
        if (driver == null || driver.getRole() != User.Role.DRIVER) {
            return java.util.Map.of("error", "Unauthorized");
        }
        
        // Get driver statistics
        List<Booking> driverBookings = bookingService.findByUser(driver);
        long activeBookings = driverBookings.stream()
            .filter(booking -> booking.getStatus() == Booking.BookingStatus.ACTIVE)
            .count();
        long totalBookings = driverBookings.size();
        long completedBookings = driverBookings.stream()
            .filter(booking -> booking.getStatus() == Booking.BookingStatus.COMPLETED)
            .count();
        
        // Get slot statistics
        List<Slot> allSlots = slotService.getAllSlots();
        long availableSlots = allSlots.stream()
            .filter(slot -> slot.getStatus() == Slot.SlotStatus.AVAILABLE)
            .count();
        long occupiedSlots = allSlots.stream()
            .filter(slot -> slot.getStatus() == Slot.SlotStatus.OCCUPIED)
            .count();
        
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("activeBookings", activeBookings);
        data.put("totalBookings", totalBookings);
        data.put("completedBookings", completedBookings);
        data.put("availableSlots", availableSlots);
        data.put("occupiedSlots", occupiedSlots);
        data.put("totalSlots", allSlots.size());
        data.put("lastUpdated", java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm:ss")));
        
        return data;
    }
    
    @PostMapping("/bookings/create")
    public String createBooking(@RequestParam Long slotId,
                               @RequestParam String startTime,
                               @RequestParam String endTime,
                               @RequestParam String vehicleNumber,
                               Principal principal,
                               RedirectAttributes redirectAttributes) {
        try {
            User driver = userService.findByUsername(principal.getName()).orElse(null);
            if (driver == null || driver.getRole() != User.Role.DRIVER) {
                return "redirect:/login";
            }
            
            LocalDateTime start = LocalDateTime.parse(startTime);
            LocalDateTime end = LocalDateTime.parse(endTime);
            
            Booking booking = bookingService.createBooking(driver, slotId, start, end, vehicleNumber);
            redirectAttributes.addFlashAttribute("success", "Booking created successfully!");
            
            // Redirect to payment page for the new booking
            return "redirect:/payment/checkout/" + booking.getId();
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Booking failed: " + e.getMessage());
            return "redirect:/driver/slots";
        }
    }
    
    @GetMapping("/bookings")
    public String viewBookings(Principal principal, Model model) {
        User driver = userService.findByUsername(principal.getName()).orElse(null);
        if (driver == null || driver.getRole() != User.Role.DRIVER) {
            return "redirect:/login";
        }
        
        List<Booking> bookings = bookingService.findByUser(driver);
        model.addAttribute("bookings", bookings);
        model.addAttribute("driver", driver);
        model.addAttribute("bookingService", bookingService);
        return "driver/bookings";
    }
    
    @PostMapping("/bookings/cancel")
    public String cancelBooking(@RequestParam Long bookingId,
                               Principal principal,
                               RedirectAttributes redirectAttributes) {
        try {
            User driver = userService.findByUsername(principal.getName()).orElse(null);
            if (driver == null || driver.getRole() != User.Role.DRIVER) {
                return "redirect:/login";
            }
            
            bookingService.cancelBooking(bookingId);
            redirectAttributes.addFlashAttribute("success", "Booking cancelled successfully!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Cancellation failed: " + e.getMessage());
        }
        
        return "redirect:/driver/bookings";
    }
    
    @GetMapping("/bookings/{bookingId}/cancellation-status")
    @ResponseBody
    public java.util.Map<String, Object> getCancellationStatus(@PathVariable Long bookingId, Principal principal) {
        try {
            User driver = userService.findByUsername(principal.getName()).orElse(null);
            if (driver == null || driver.getRole() != User.Role.DRIVER) {
                return java.util.Map.of("error", "Unauthorized");
            }
            
            Booking booking = bookingService.findById(bookingId).orElse(null);
            if (booking == null || !booking.getUser().getId().equals(driver.getId())) {
                return java.util.Map.of("error", "Booking not found");
            }
            
            boolean canCancel = bookingService.canCancelBooking(booking);
            long timeRemaining = bookingService.getCancellationTimeRemaining(booking);
            
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("canCancel", canCancel);
            response.put("timeRemaining", timeRemaining);
            response.put("status", booking.getStatus().toString());
            
            return response;
        } catch (Exception e) {
            return java.util.Map.of("error", e.getMessage());
        }
    }
    
    @GetMapping("/profile")
    public String viewProfile(Principal principal, Model model) {
        User driver = userService.findByUsername(principal.getName()).orElse(null);
        if (driver == null || driver.getRole() != User.Role.DRIVER) {
            return "redirect:/login";
        }
        
        model.addAttribute("driver", driver);
        return "driver/profile";
    }
    
    @GetMapping("/profile-edit")
    public String editProfile(Principal principal, Model model) {
        User driver = userService.findByUsername(principal.getName()).orElse(null);
        if (driver == null || driver.getRole() != User.Role.DRIVER) {
            return "redirect:/login";
        }
        
        model.addAttribute("user", driver);
        return "driver/profile-edit";
    }
    
    @PostMapping("/profile/update")
    public String updateProfile(@Valid @ModelAttribute("user") User user,
                               BindingResult bindingResult,
                               Principal principal,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        
        User currentDriver = userService.findByUsername(principal.getName()).orElse(null);
        if (currentDriver == null || currentDriver.getRole() != User.Role.DRIVER) {
            return "redirect:/login";
        }
        
        if (bindingResult.hasErrors()) {
            return "driver/edit-profile";
        }
        
        try {
            // Keep the same ID and role
            user.setId(currentDriver.getId());
            user.setRole(User.Role.DRIVER);
            user.setPassword(currentDriver.getPassword()); // Keep existing password
            user.setEnabled(currentDriver.isEnabled());
            
            userService.updateUser(user);
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
            return "redirect:/driver/profile";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "driver/edit-profile";
        }
    }
    
    @GetMapping("/change-password")
    public String changePasswordPage(Model model) {
        return "driver/change-password";
    }
    
    @PostMapping("/change-password")
    public String changePassword(@RequestParam String currentPassword,
                                @RequestParam String newPassword,
                                @RequestParam String confirmPassword,
                                Principal principal,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        
        User driver = userService.findByUsername(principal.getName()).orElse(null);
        if (driver == null || driver.getRole() != User.Role.DRIVER) {
            return "redirect:/login";
        }
        
        // Validate current password
        if (!userService.validatePassword(currentPassword, driver.getPassword())) {
            model.addAttribute("error", "Current password is incorrect");
            return "driver/change-password";
        }
        
        // Validate new password
        if (newPassword.length() < 6) {
            model.addAttribute("error", "New password must be at least 6 characters");
            return "driver/change-password";
        }
        
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "New passwords do not match");
            return "driver/change-password";
        }
        
        try {
            driver.setPassword(passwordEncoder.encode(newPassword));
            userService.updateUser(driver);
            redirectAttributes.addFlashAttribute("success", "Password changed successfully!");
            return "redirect:/driver/profile";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "driver/change-password";
        }
    }
}
