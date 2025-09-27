package com.example.park.repository;

import com.example.park.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    boolean existsByUsername(String username);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = 'DRIVER'")
    long countUsers();
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = 'ADMIN'")
    long countAdmins();
}
