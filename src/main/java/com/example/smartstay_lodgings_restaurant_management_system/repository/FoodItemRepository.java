package com.example.smartstay_lodgings_restaurant_management_system.repository;

import com.example.smartstay_lodgings_restaurant_management_system.model.FoodItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FoodItemRepository extends JpaRepository<FoodItem, Long> {
    List<FoodItem> findByAvailableTrueOrderByNameAsc();
}

