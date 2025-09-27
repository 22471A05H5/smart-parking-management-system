package com.example.park.config;

import com.example.park.entity.User;
import com.example.park.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        // Create default admin user if not exists
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("Admin@123"));
            admin.setRole(User.Role.ADMIN);
            admin.setEnabled(true);
            
            userRepository.save(admin);
            System.out.println("Default admin user created: admin/Admin@123");
        }
    }
}
