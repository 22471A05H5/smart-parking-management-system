package com.example.park.controller;

import com.example.park.entity.Booking;
import com.example.park.service.BookingService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.PostConstruct;

import java.math.BigDecimal;
import java.util.Optional;

@Controller
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private BookingService bookingService;

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @Value("${stripe.publishable.key}")
    private String stripePublishableKey;

    @PostConstruct
    private void init() {
        Stripe.apiKey = stripeApiKey;
    }

    @GetMapping("/checkout/{bookingId}")
    public String showCheckout(@PathVariable Long bookingId, Model model) {
        Optional<Booking> bookingOpt = bookingService.findById(bookingId);
        
        if (bookingOpt.isEmpty()) {
            return "redirect:/driver/bookings?error=booking_not_found";
        }
        
        Booking booking = bookingOpt.get();
        model.addAttribute("booking", booking);
        model.addAttribute("stripePublishableKey", stripePublishableKey);
        
        return "payment/checkout";
    }

    @PostMapping("/create-checkout-session")
    @ResponseBody
    public String createCheckoutSession(@RequestParam Long bookingId) {
        try {
            Optional<Booking> bookingOpt = bookingService.findById(bookingId);
            
            if (bookingOpt.isEmpty()) {
                return "{\"error\": \"Booking not found\"}";
            }
            
            Booking booking = bookingOpt.get();
            
            // Ensure minimum amount for Stripe (₹50 minimum to meet $0.50 USD requirement)
            BigDecimal originalAmount = booking.getTotalAmount();
            BigDecimal minimumAmount = new BigDecimal("50.00"); // ₹50 minimum
            BigDecimal finalAmount = originalAmount.compareTo(minimumAmount) < 0 ? minimumAmount : originalAmount;
            
            // Convert amount to cents for Stripe (multiply by 100)
            long amountInCents = finalAmount.multiply(new BigDecimal("100")).longValue();
            
            SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("http://localhost:8091/payment/success?session_id={CHECKOUT_SESSION_ID}&booking_id=" + bookingId)
                .setCancelUrl("http://localhost:8091/payment/cancel?booking_id=" + bookingId)
                .addLineItem(
                    SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(
                            SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("inr")
                                .setUnitAmount(amountInCents)
                                .setProductData(
                                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName("Parking Slot Booking - " + booking.getSlot().getSlotNumber())
                                        .setDescription("Zone: " + booking.getSlot().getZone() + 
                                                      " | Vehicle: " + booking.getVehicleNumber() +
                                                      " | Duration: " + booking.getStartTime() + " to " + booking.getEndTime() +
                                                      (finalAmount.compareTo(originalAmount) > 0 ? 
                                                       " | Minimum charge applied (₹" + finalAmount + ")" : ""))
                                        .build()
                                )
                                .build()
                        )
                        .build()
                )
                .build();

            Session session = Session.create(params);
            return "{\"sessionId\": \"" + session.getId() + "\"}";
            
        } catch (StripeException e) {
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }

    @GetMapping("/success")
    public String paymentSuccess(@RequestParam String session_id, 
                               @RequestParam Long booking_id, 
                               Model model) {
        try {
            // Verify the session with Stripe
            Session session = Session.retrieve(session_id);
            
            if ("complete".equals(session.getStatus())) {
                // Update booking payment status
                bookingService.updatePaymentStatus(booking_id, Booking.PaymentStatus.PAID);
                
                Optional<Booking> bookingOpt = bookingService.findById(booking_id);
                model.addAttribute("booking", bookingOpt.orElse(null));
                model.addAttribute("sessionId", session_id);
                
                return "payment/success";
            } else {
                return "redirect:/payment/cancel?booking_id=" + booking_id;
            }
            
        } catch (StripeException e) {
            model.addAttribute("error", "Payment verification failed: " + e.getMessage());
            return "payment/error";
        }
    }

    @GetMapping("/cancel")
    public String paymentCancel(@RequestParam Long booking_id, Model model) {
        Optional<Booking> bookingOpt = bookingService.findById(booking_id);
        model.addAttribute("booking", bookingOpt.orElse(null));
        return "payment/cancel";
    }

    @GetMapping("/error")
    public String paymentError(Model model) {
        return "payment/error";
    }
}
