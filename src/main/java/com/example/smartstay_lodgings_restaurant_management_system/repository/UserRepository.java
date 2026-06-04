package com.example.smartstay_lodgings_restaurant_management_system.repository;

import com.example.smartstay_lodgings_restaurant_management_system.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}

