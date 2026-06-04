package com.example.smartstay_lodgings_restaurant_management_system.repository;

import com.example.smartstay_lodgings_restaurant_management_system.model.RestaurantOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RestaurantOrderRepository extends JpaRepository<RestaurantOrder, Long> {

    long countByOrderStatus(String status);

    @Query(value = "SELECT food_item, SUM(quantity) as qty FROM restaurant_orders GROUP BY food_item ORDER BY qty DESC LIMIT 5", nativeQuery = true)
    List<Object[]> findTopFoodItems();

    @Query(value = "SELECT food_category, COALESCE(SUM(amount),0) FROM restaurant_orders GROUP BY food_category ORDER BY 1", nativeQuery = true)
    List<Object[]> getCategoryTotals();

    @Query(value = "SELECT COALESCE(SUM(amount),0) FROM restaurant_orders WHERE CAST(ordered_at AS date) = :date", nativeQuery = true)
    Double sumAmountForDate(@org.springframework.data.repository.query.Param("date") java.time.LocalDate date);

    List<RestaurantOrder> findByBookingId(Long bookingId);
}

