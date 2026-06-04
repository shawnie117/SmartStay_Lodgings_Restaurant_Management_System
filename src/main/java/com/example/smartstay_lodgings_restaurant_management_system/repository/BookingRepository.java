package com.example.smartstay_lodgings_restaurant_management_system.repository;

import com.example.smartstay_lodgings_restaurant_management_system.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByQrToken(String token);

    long countByBookingStatus(String status);

    long countByRoomId(Long roomId);

    @Query(value = "SELECT CAST(created_at AS date) as d, COALESCE(SUM(total_amount),0) as amt " +
            "FROM bookings WHERE created_at >= :from GROUP BY CAST(created_at AS date) ORDER BY 1", nativeQuery = true)
    List<Object[]> getDailyRevenueSince(@Param("from") LocalDateTime from);

    @Query(value = "SELECT r.room_type, COUNT(*) FROM bookings b JOIN rooms r ON b.room_id = r.room_id " +
            "WHERE b.booking_status = 'CHECKED_IN' GROUP BY r.room_type", nativeQuery = true)
    List<Object[]> getOccupancyByType();

    List<Booking> findTop10ByOrderByCreatedAtDesc();

    @Query(value = "SELECT CAST(created_at AS date) as d, COUNT(*) FROM bookings WHERE created_at >= :from GROUP BY CAST(created_at AS date) ORDER BY 1", nativeQuery = true)
    List<Object[]> getDailyBookingCountsSince(@Param("from") LocalDateTime from);

    @Query(value = "SELECT EXTRACT(MONTH FROM created_at) as m, COALESCE(SUM(total_amount),0) FROM bookings WHERE EXTRACT(YEAR FROM created_at) = :year GROUP BY m ORDER BY m", nativeQuery = true)
    List<Object[]> getMonthlyRevenue(@Param("year") int year);

    @Query(value = "SELECT EXTRACT(MONTH FROM created_at) as m, COUNT(DISTINCT customer_name) FROM bookings WHERE EXTRACT(YEAR FROM created_at) = :year GROUP BY m ORDER BY m", nativeQuery = true)
    List<Object[]> getMonthlyGuestCounts(@Param("year") int year);

    @Query(value = "SELECT COALESCE(SUM(total_amount),0) FROM bookings WHERE CAST(created_at AS date) = :date", nativeQuery = true)
    Double sumRevenueForDate(@Param("date") java.time.LocalDate date);

    @Query(value = "SELECT COALESCE(AVG((checkout_date - checkin_date)),0) FROM bookings WHERE checkout_date IS NOT NULL AND checkin_date IS NOT NULL", nativeQuery = true)
    Double getAverageStayDurationDays();

    @Query(value = "SELECT COUNT(DISTINCT customer_name) FROM bookings WHERE DATE_TRUNC('month', created_at) = DATE_TRUNC('month', CURRENT_DATE)", nativeQuery = true)
    Long getGuestsCountMonthToDate();

    @Query(value = "SELECT b.* FROM bookings b JOIN rooms r ON b.room_id = r.room_id " +
            "WHERE r.room_number = :roomNumber AND b.guest_pass = :guestPass " +
            "AND b.booking_status IN ('CONFIRMED','CHECKED_IN') ORDER BY b.created_at DESC LIMIT 1", nativeQuery = true)
    Optional<Booking> findActiveByRoomNumberAndGuestPass(@Param("roomNumber") String roomNumber,
                                                         @Param("guestPass") String guestPass);
}
