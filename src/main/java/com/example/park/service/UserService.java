package com.example.park.service;

import com.example.park.entity.User;
import com.example.park.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    public User registerUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(User.Role.DRIVER);
        user.setEnabled(true);
        
        return userRepository.save(user);
    }
    
    public User createAdmin(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(User.Role.ADMIN);
        user.setEnabled(true);
        
        return userRepository.save(user);
    }
    
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
    
    @Transactional(readOnly = true)
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }
    
    public User updateUser(User user) {
        return userRepository.save(user);
    }
    
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
    
    public boolean validatePassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
    
    @Transactional(readOnly = true)
    public long getTotalUsersCount() {
        return userRepository.countUsers();
    }
    
    @Transactional(readOnly = true)
    public long getTotalAdminsCount() {
        return userRepository.countAdmins();
    }
    
    @Transactional(readOnly = true)
    public List<User> findAllDrivers() {
        return userRepository.findAll().stream()
                .filter(user -> user.getRole() == User.Role.DRIVER)
                .toList();
    }
}
