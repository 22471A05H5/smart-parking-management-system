package com.example.park.config;

import com.example.park.entity.User;
import com.example.park.repository.UserRepository;
import com.example.park.service.SlotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private SlotService slotService;
    
    @Override
    public void run(String... args) throws Exception {
        try {
            // Create default admin user if not exists
            if (!userRepository.existsByUsername("admin")) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("Admin@123"));
                admin.setRole(User.Role.ADMIN);
                admin.setEnabled(true);
                
                userRepository.save(admin);
                System.out.println("Default admin user created: admin / Admin@123");
            }
            
            // Initialize default parking slots
            slotService.initializeDefaultSlots();
            
            System.out.println("Application startup completed successfully!");
        } catch (Exception e) {
            System.err.println("Error during application startup: " + e.getMessage());
            e.printStackTrace();
            // Don't rethrow - let application continue
        }
    }
}
