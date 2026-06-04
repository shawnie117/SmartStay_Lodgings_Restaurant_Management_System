package com.example.smartstay_lodgings_restaurant_management_system.config;

import com.example.smartstay_lodgings_restaurant_management_system.model.User;
import com.example.smartstay_lodgings_restaurant_management_system.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        try {
            // Check if admin user exists
            var existingAdmin = userRepository.findByEmail("admin@smartstay.com");
            if (existingAdmin.isEmpty()) {
                User admin = new User();
                admin.setEmail("admin@smartstay.com");
                admin.setName("Admin Kumar");
                admin.setPassword(passwordEncoder.encode("Admin@123"));
                admin.setRole("ADMIN");
                userRepository.save(admin);
                logger.info("✓ Admin user created: admin@smartstay.com");
            } else {
                logger.info("✓ Admin user already exists");
            }
        } catch (Exception e) {
            logger.error("✗ Error initializing admin user", e);
        }
    }
}

