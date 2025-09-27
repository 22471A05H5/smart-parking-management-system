package com.example.park.controller;

import com.example.park.entity.User;
import com.example.park.entity.Booking;
import com.example.park.service.UserService;
import com.example.park.service.SlotService;
import com.example.park.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private SlotService slotService;
    
    @Autowired
    private BookingService bookingService;
    
    @GetMapping("/dashboard")
    public String adminDashboard(Principal principal, Model model) {
        User admin = userService.findByUsername(principal.getName()).orElse(null);
        if (admin == null || admin.getRole() != User.Role.ADMIN) {
            return "redirect:/login";
        }
        
        // Get real-time statistics
        long totalSlots = slotService.getTotalSlotsCount();
        long availableSlots = slotService.getAvailableSlotsCount();
        long occupiedSlots = slotService.getOccupiedSlotsCount();
        long reservedSlots = slotService.getReservedSlotsCount();
        long activeBookings = bookingService.getActiveBookingsCount();
        Double todayRevenue = bookingService.getTodayRevenue();
        
        // Get all drivers
        List<User> drivers = userService.findAllDrivers();
        
        model.addAttribute("admin", admin);
        model.addAttribute("drivers", drivers);
        model.addAttribute("totalDrivers", drivers.size());
        model.addAttribute("totalSlots", totalSlots);
        model.addAttribute("availableSlots", availableSlots);
        model.addAttribute("occupiedSlots", occupiedSlots);
        model.addAttribute("reservedSlots", reservedSlots);
        model.addAttribute("activeBookings", activeBookings);
        model.addAttribute("todayRevenue", String.format("₹%.2f", todayRevenue));
        
        return "admin/dashboard";
    }
    
    @GetMapping("/drivers")
    public String manageDrivers(Model model) {
        List<User> drivers = userService.findAllDrivers();
        model.addAttribute("drivers", drivers);
        return "admin/drivers";
    }
    
    @GetMapping("/bookings")
    public String manageBookings(Model model) {
        List<Booking> bookings = bookingService.getAllBookings();
        
        // Calculate today's bookings count
        long todayBookings = bookings.stream()
            .filter(booking -> booking.getCreatedAt().toLocalDate().equals(java.time.LocalDate.now()))
            .count();
        
        model.addAttribute("bookings", bookings);
        model.addAttribute("todayBookings", todayBookings);
        return "admin/bookings";
    }
    
    @GetMapping("/users/{userId}/bookings")
    public String viewUserBookings(@PathVariable Long userId, Model model) {
        User user = userService.findById(userId).orElse(null);
        if (user == null) {
            return "redirect:/admin/drivers";
        }
        
        List<com.example.park.entity.Booking> userBookings = bookingService.findByUser(user);
        model.addAttribute("user", user);
        model.addAttribute("bookings", userBookings);
        return "admin/user-bookings";
    }
    
    @GetMapping("/drivers/add")
    public String addDriverPage(Model model) {
        model.addAttribute("user", new User());
        return "admin/add-driver";
    }
    
    @PostMapping("/drivers/add")
    public String addDriver(@Valid @ModelAttribute("user") User user,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes,
                           Model model) {
        if (bindingResult.hasErrors()) {
            return "admin/add-driver";
        }
        
        try {
            user.setRole(User.Role.DRIVER);
            userService.registerUser(user);
            redirectAttributes.addFlashAttribute("success", "Driver added successfully!");
            return "redirect:/admin/drivers";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "admin/add-driver";
        }
    }
    
    @GetMapping("/drivers/edit/{id}")
    public String editDriverPage(@PathVariable Long id, Model model) {
        User driver = userService.findById(id).orElse(null);
        if (driver == null || driver.getRole() != User.Role.DRIVER) {
            return "redirect:/admin/drivers";
        }
        model.addAttribute("user", driver);
        return "admin/edit-driver";
    }
    
    @PostMapping("/drivers/edit/{id}")
    public String editDriver(@PathVariable Long id,
                            @Valid @ModelAttribute("user") User user,
                            BindingResult bindingResult,
                            RedirectAttributes redirectAttributes,
                            Model model) {
        if (bindingResult.hasErrors()) {
            return "admin/edit-driver";
        }
        
        try {
            user.setId(id);
            user.setRole(User.Role.DRIVER);
            userService.updateUser(user);
            redirectAttributes.addFlashAttribute("success", "Driver updated successfully!");
            return "redirect:/admin/drivers";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "admin/edit-driver";
        }
    }
    
    @PostMapping("/drivers/delete/{id}")
    public String deleteDriver(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            User driver = userService.findById(id).orElse(null);
            if (driver != null && driver.getRole() == User.Role.DRIVER) {
                userService.deleteUser(id);
                redirectAttributes.addFlashAttribute("success", "Driver deleted successfully!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Driver not found!");
            }
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/drivers";
    }
    
    @GetMapping("/slots")
    public String manageSlots(Model model) {
        // Get real-time statistics
        long totalSlots = slotService.getTotalSlotsCount();
        long availableSlots = slotService.getAvailableSlotsCount();
        long occupiedSlots = slotService.getOccupiedSlotsCount();
        long reservedSlots = slotService.getReservedSlotsCount();
        
        // Get all slots
        List<com.example.park.entity.Slot> slots = slotService.getAllSlots();
        
        model.addAttribute("totalSlots", totalSlots);
        model.addAttribute("availableSlots", availableSlots);
        model.addAttribute("occupiedSlots", occupiedSlots);
        model.addAttribute("reservedSlots", reservedSlots);
        model.addAttribute("slots", slots);
        
        return "admin/slots";
    }
    
    @PostMapping("/slots/add")
    public String addSlot(@RequestParam String slotNumber,
                         @RequestParam String zone,
                         @RequestParam Double pricePerHour,
                         RedirectAttributes redirectAttributes) {
        try {
            slotService.createSlot(slotNumber, zone, pricePerHour);
            redirectAttributes.addFlashAttribute("success", "Slot created successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to add slot: " + e.getMessage());
        }
        return "redirect:/admin/slots";
    }
    
    @GetMapping("/slots/{id}")
    @ResponseBody
    public com.example.park.entity.Slot getSlot(@PathVariable Long id) {
        return slotService.findById(id).orElse(null);
    }
    
    @PostMapping("/slots/edit/{id}")
    public String editSlot(@PathVariable Long id,
                          @RequestParam String slotNumber,
                          @RequestParam String zone,
                          @RequestParam Double pricePerHour,
                          @RequestParam String status,
                          RedirectAttributes redirectAttributes) {
        try {
            slotService.updateSlot(id, slotNumber, zone, pricePerHour, status);
            redirectAttributes.addFlashAttribute("success", "Slot updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update slot: " + e.getMessage());
        }
        return "redirect:/admin/slots";
    }
    
    @PostMapping("/slots/delete/{id}")
    public String deleteSlot(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            slotService.deleteSlot(id);
            redirectAttributes.addFlashAttribute("success", "Slot deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete slot: " + e.getMessage());
        }
        return "redirect:/admin/slots";
    }
    
    @GetMapping("/parking-slots")
    public String manageParkingSlots(Model model) {
        // Get real-time statistics
        long totalSlots = slotService.getTotalSlotsCount();
        long availableSlots = slotService.getAvailableSlotsCount();
        long occupiedSlots = slotService.getOccupiedSlotsCount();
        long reservedSlots = slotService.getReservedSlotsCount();
        
        // Get all slots
        List<com.example.park.entity.Slot> slots = slotService.getAllSlots();
        
        model.addAttribute("totalSlots", totalSlots);
        model.addAttribute("availableSlots", availableSlots);
        model.addAttribute("occupiedSlots", occupiedSlots);
        model.addAttribute("reservedSlots", reservedSlots);
        model.addAttribute("slots", slots);
        
        return "admin/parking-slots";
    }
    
    
    @GetMapping("/payments")
    public String viewPayments(Model model) {
        List<Booking> allBookings = bookingService.getAllBookings();
        
        // Calculate total revenue from all paid bookings
        java.math.BigDecimal totalRevenue = allBookings.stream()
            .filter(booking -> booking.getPaymentStatus() == Booking.PaymentStatus.PAID)
            .map(Booking::getTotalAmount)
            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        
        // Calculate today's revenue
        java.math.BigDecimal todayRevenue = allBookings.stream()
            .filter(booking -> booking.getPaymentStatus() == Booking.PaymentStatus.PAID)
            .filter(booking -> booking.getCreatedAt().toLocalDate().equals(java.time.LocalDate.now()))
            .map(Booking::getTotalAmount)
            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        
        // Calculate monthly revenue
        java.math.BigDecimal monthlyRevenue = allBookings.stream()
            .filter(booking -> booking.getPaymentStatus() == Booking.PaymentStatus.PAID)
            .filter(booking -> booking.getCreatedAt().getMonth().equals(java.time.LocalDateTime.now().getMonth()))
            .map(Booking::getTotalAmount)
            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        
        // Get only paid bookings as payments
        List<Booking> payments = allBookings.stream()
            .filter(booking -> booking.getPaymentStatus() == Booking.PaymentStatus.PAID)
            .collect(java.util.stream.Collectors.toList());
        
        // Calculate payment status counts
        long paidPayments = allBookings.stream()
            .filter(booking -> booking.getPaymentStatus() == Booking.PaymentStatus.PAID)
            .count();
        long pendingPayments = allBookings.stream()
            .filter(booking -> booking.getPaymentStatus() == Booking.PaymentStatus.PENDING)
            .count();
        long failedPayments = allBookings.stream()
            .filter(booking -> booking.getPaymentStatus() == Booking.PaymentStatus.FAILED)
            .count();
        
        model.addAttribute("payments", payments);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("todayRevenue", todayRevenue);
        model.addAttribute("monthlyRevenue", monthlyRevenue);
        model.addAttribute("paidPayments", paidPayments);
        model.addAttribute("pendingPayments", pendingPayments);
        model.addAttribute("failedPayments", failedPayments);
        return "admin/payments";
    }
}
