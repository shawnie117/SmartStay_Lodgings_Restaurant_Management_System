package com.example.smartstay_lodgings_restaurant_management_system.repository;

import com.example.smartstay_lodgings_restaurant_management_system.model.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Long> {
}

