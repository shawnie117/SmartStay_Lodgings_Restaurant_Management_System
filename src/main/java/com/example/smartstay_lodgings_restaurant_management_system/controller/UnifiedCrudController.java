package com.example.smartstay_lodgings_restaurant_management_system.controller;

import com.example.smartstay_lodgings_restaurant_management_system.model.Booking;
import com.example.smartstay_lodgings_restaurant_management_system.model.RestaurantOrder;
import com.example.smartstay_lodgings_restaurant_management_system.model.Room;
import com.example.smartstay_lodgings_restaurant_management_system.model.FoodItem;
import com.example.smartstay_lodgings_restaurant_management_system.model.Staff;
import com.example.smartstay_lodgings_restaurant_management_system.repository.BookingRepository;
import com.example.smartstay_lodgings_restaurant_management_system.repository.RestaurantOrderRepository;
import com.example.smartstay_lodgings_restaurant_management_system.repository.RoomRepository;
import com.example.smartstay_lodgings_restaurant_management_system.repository.FoodItemRepository;
import com.example.smartstay_lodgings_restaurant_management_system.repository.StaffRepository;
import com.example.smartstay_lodgings_restaurant_management_system.service.GroqService;
import com.example.smartstay_lodgings_restaurant_management_system.model.User;
import com.example.smartstay_lodgings_restaurant_management_system.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class UnifiedCrudController {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RestaurantOrderRepository orderRepository;

    @Autowired
    private GroqService groqService;

    @Autowired
    private FoodItemRepository foodItemRepository;

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Simple landing page mapping
    @GetMapping({"/", "/index.html"})
    public String index(Model model) {
        long totalRooms = roomRepository.count();
        long available = roomRepository.countByStatus("AVAILABLE");
        long occupied = roomRepository.countByStatus("OCCUPIED");
        model.addAttribute("totalRooms", totalRooms);
        model.addAttribute("availableRooms", available);
        model.addAttribute("occupiedRooms", occupied);
        return "index";
    }

    // Rooms - Thymeleaf view
    @GetMapping({"/rooms", "/rooms.html"})
    public String roomsPage(Model model) {
        long totalRooms = roomRepository.count();
        long availableRooms = roomRepository.countByStatus("AVAILABLE");
        long occupiedRooms = roomRepository.countByStatus("OCCUPIED");
        long maintenanceRooms = roomRepository.countByStatus("MAINTENANCE");
        model.addAttribute("rooms", roomRepository.findAll());
        model.addAttribute("totalRooms", totalRooms);
        model.addAttribute("availableRooms", availableRooms);
        model.addAttribute("occupiedRooms", occupiedRooms);
        model.addAttribute("maintenanceRooms", maintenanceRooms);
        return "rooms";
    }

    @GetMapping({"/dashboard", "/dashboard.html"})
    public String dashboardPage(Model model) {
        long totalRooms = roomRepository.count();
        long availableRooms = roomRepository.countByStatus("AVAILABLE");
        long occupiedRooms = roomRepository.countByStatus("OCCUPIED");
        long maintenanceRooms = roomRepository.countByStatus("MAINTENANCE");

        long activeBookings = bookingRepository.countByBookingStatus("CHECKED_IN");
        Double roomsRevenue = bookingRepository.sumRevenueForDate(java.time.LocalDate.now());
        Double restaurantRevenue = orderRepository.sumAmountForDate(java.time.LocalDate.now());
        double roomsRevenueVal = roomsRevenue == null ? 0.0 : roomsRevenue;
        double restaurantRevenueVal = restaurantRevenue == null ? 0.0 : restaurantRevenue;

        double occupancyPercent = totalRooms > 0 ? (occupiedRooms * 100.0 / totalRooms) : 0.0;

        model.addAttribute("totalRooms", totalRooms);
        model.addAttribute("occupiedRooms", occupiedRooms);
        model.addAttribute("availableRooms", availableRooms);
        model.addAttribute("maintenanceRooms", maintenanceRooms);
        model.addAttribute("activeBookings", activeBookings);
        model.addAttribute("todaysRevenue", roomsRevenueVal + restaurantRevenueVal);
        model.addAttribute("occupancyPercent", Math.round(occupancyPercent));

        model.addAttribute("revenueLabels", List.of("Rooms", "Restaurant"));
        model.addAttribute("revenueValues", List.of(roomsRevenueVal, restaurantRevenueVal));
        model.addAttribute("occupancyLabels", List.of("Available", "Occupied", "Maintenance"));
        model.addAttribute("occupancyValues", List.of(availableRooms, occupiedRooms, maintenanceRooms));

        model.addAttribute("recentBookings", bookingRepository.findTop10ByOrderByCreatedAtDesc());
        return "dashboard";
    }

    @GetMapping({"/bookings", "/bookings.html"})
    public String bookingsPage(Model model) {
        model.addAttribute("bookings", bookingRepository.findAll());
        model.addAttribute("rooms", roomRepository.findAll());
        return "bookings";
    }

    @GetMapping({"/restaurant", "/restaurant.html"})
    public String restaurantPage(Model model) {
        List<RestaurantOrder> orders = orderRepository.findAll();
        model.addAttribute("pendingOrders", orders.stream().filter(o -> "PENDING".equalsIgnoreCase(o.getOrderStatus())).toList());
        model.addAttribute("preparingOrders", orders.stream().filter(o -> "PREPARING".equalsIgnoreCase(o.getOrderStatus())).toList());
        model.addAttribute("completedOrders", orders.stream().filter(o -> "COMPLETED".equalsIgnoreCase(o.getOrderStatus())).toList());
        model.addAttribute("bookings", bookingRepository.findAll());
        model.addAttribute("menuItems", foodItemRepository.findAll());
        return "restaurant";
    }

     @GetMapping({"/billing", "/billing.html"})
     public String billingPage(@RequestParam(required = false) Long bookingId, Model model) {
         List<Booking> bookings = bookingRepository.findAll();
         Booking selectedBooking = null;

         if (bookingId != null) {
             selectedBooking = bookingRepository.findById(bookingId).orElse(null);
         }
         if (selectedBooking == null && !bookings.isEmpty()) {
             selectedBooking = bookings.get(0);
         }

         model.addAttribute("bookings", bookings);
         model.addAttribute("selectedBooking", selectedBooking);
         model.addAttribute("today", java.time.LocalDate.now());

         if (selectedBooking != null) {
             Double roomCharge = selectedBooking.getTotalAmount() != null ? selectedBooking.getTotalAmount() : 0.0;

             List<RestaurantOrder> orders = orderRepository.findByBookingId(selectedBooking.getId());
             Double foodTotal = orders.stream()
                     .mapToDouble(o -> o.getAmount() * (o.getQuantity() == null ? 1 : o.getQuantity()))
                     .sum();

             long dayCount = 0;
             double ratePerDay = 0.0;
             if (selectedBooking.getCheckinDate() != null && selectedBooking.getCheckoutDate() != null) {
                 dayCount = java.time.temporal.ChronoUnit.DAYS.between(selectedBooking.getCheckinDate(), selectedBooking.getCheckoutDate());
                 if (dayCount > 0) {
                     ratePerDay = roomCharge / dayCount;
                 }
             }

             Double subtotal = roomCharge + foodTotal;
             Double gstOnRooms = roomCharge * 0.12;
             Double gstOnFood = foodTotal * 0.05;
             Double grandTotal = subtotal + gstOnRooms + gstOnFood;

             model.addAttribute("roomCharge", roomCharge);
             model.addAttribute("foodTotal", foodTotal);
             model.addAttribute("subtotal", subtotal);
             model.addAttribute("gstOnRooms", gstOnRooms);
             model.addAttribute("gstOnFood", gstOnFood);
             model.addAttribute("grandTotal", grandTotal);
             model.addAttribute("orders", orders);
             model.addAttribute("dayCount", dayCount);
             model.addAttribute("ratePerDay", ratePerDay);
         } else {
             model.addAttribute("orders", java.util.List.of());
             model.addAttribute("roomCharge", 0.0);
             model.addAttribute("foodTotal", 0.0);
             model.addAttribute("subtotal", 0.0);
             model.addAttribute("gstOnRooms", 0.0);
             model.addAttribute("gstOnFood", 0.0);
             model.addAttribute("grandTotal", 0.0);
             model.addAttribute("dayCount", 0);
             model.addAttribute("ratePerDay", 0.0);
         }

         return "billing";
     }

    @GetMapping({"/reports", "/reports.html"})
    public String reportsPage(Model model) {
        LocalDateTime from = LocalDateTime.now().minusDays(30);
        List<Object[]> dailyRevenue = bookingRepository.getDailyRevenueSince(from);
        List<String> revenueDates = dailyRevenue.stream().map(r -> String.valueOf(r[0])).collect(Collectors.toList());
        List<Double> revenueAmounts = dailyRevenue.stream().map(r -> toDouble(r[1])).collect(Collectors.toList());

        int year = java.time.Year.now().getValue();
        List<Object[]> monthlyRevenue = bookingRepository.getMonthlyRevenue(year);
        Map<Integer, Double> monthlyRevenueMap = new HashMap<>();
        for (Object[] row : monthlyRevenue) {
            monthlyRevenueMap.put(toInt(row[0]), toDouble(row[1]));
        }
        List<String> monthLabels = List.of("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec");
        List<Double> monthValues = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            monthValues.add(monthlyRevenueMap.getOrDefault(i, 0.0));
        }

        List<Object[]> occupancyByType = bookingRepository.getOccupancyByType();
        List<String> occupancyTypes = occupancyByType.stream().map(r -> String.valueOf(r[0])).collect(Collectors.toList());
        List<Double> occupancyCounts = occupancyByType.stream().map(r -> toDouble(r[1])).collect(Collectors.toList());

        List<Object[]> occupancyTrendRaw = bookingRepository.getDailyBookingCountsSince(from);
        List<String> occupancyTrendDates = occupancyTrendRaw.stream().map(r -> String.valueOf(r[0])).collect(Collectors.toList());
        List<Double> occupancyTrendValues = occupancyTrendRaw.stream().map(r -> toDouble(r[1])).collect(Collectors.toList());

        List<Object[]> categoryTotals = orderRepository.getCategoryTotals();
        List<String> categoryLabels = categoryTotals.stream().map(r -> String.valueOf(r[0])).collect(Collectors.toList());
        List<Double> categoryValues = categoryTotals.stream().map(r -> toDouble(r[1])).collect(Collectors.toList());

        List<Object[]> topItems = orderRepository.findTopFoodItems();
        List<String> topItemLabels = topItems.stream().map(r -> String.valueOf(r[0])).collect(Collectors.toList());
        List<Double> topItemValues = topItems.stream().map(r -> toDouble(r[1])).collect(Collectors.toList());

        List<Object[]> newGuests = bookingRepository.getMonthlyGuestCounts(year);
        Map<Integer, Double> guestMap = new HashMap<>();
        for (Object[] row : newGuests) {
            guestMap.put(toInt(row[0]), toDouble(row[1]));
        }
        List<Double> guestCounts = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            guestCounts.add(guestMap.getOrDefault(i, 0.0));
        }

        model.addAttribute("revenueDates", revenueDates);
        model.addAttribute("revenueAmounts", revenueAmounts);
        model.addAttribute("monthlyLabels", monthLabels);
        model.addAttribute("monthlyAmounts", monthValues);
        model.addAttribute("occupancyTypes", occupancyTypes);
        model.addAttribute("occupancyCounts", occupancyCounts);
        model.addAttribute("occupancyTrendDates", occupancyTrendDates);
        model.addAttribute("occupancyTrendValues", occupancyTrendValues);
        model.addAttribute("restaurantCategories", categoryLabels);
        model.addAttribute("restaurantCategoryTotals", categoryValues);
        model.addAttribute("topItemLabels", topItemLabels);
        model.addAttribute("topItemValues", topItemValues);
        model.addAttribute("newGuestMonths", monthLabels);
        model.addAttribute("newGuestCounts", guestCounts);

        model.addAttribute("totalGuestsMtd", bookingRepository.getGuestsCountMonthToDate());
        model.addAttribute("returnRate", 0.0);
        model.addAttribute("avgRating", 0.0);
        model.addAttribute("avgStayDuration", bookingRepository.getAverageStayDurationDays());

        model.addAttribute("startDate", java.time.LocalDate.now().minusDays(30));
        model.addAttribute("endDate", java.time.LocalDate.now());
        return "reports";
    }

    @GetMapping({"/staff", "/staff.html"})
    public String staffPage(Model model) {
        List<Staff> staffList = staffRepository.findAll();
        model.addAttribute("staff", staffList);
        model.addAttribute("totalStaff", staffList.size());
        return "staff";
    }

    @GetMapping({"/login", "/login.html"})
    public String loginPage() { return "login"; }

    @GetMapping({"/guest/login", "/guest/login.html"})
    public String guestLoginPage() { return "guest/login"; }

    @PostMapping("/guest/login")
    public String handleGuestLogin(@RequestParam("roomNumber") String roomNumber,
                                   @RequestParam("guestPass") String guestPass) {
        return bookingRepository.findActiveByRoomNumberAndGuestPass(roomNumber, guestPass)
                .map(booking -> "redirect:/guest/" + booking.getQrToken() + "/portal")
                .orElse("redirect:/guest/login?error=true");
    }

    // REST endpoints for rooms
    @GetMapping("/api/rooms")
    @ResponseBody
    public List<Room> listRooms() {
        return roomRepository.findAll();
    }

    @PostMapping("/api/rooms")
    @ResponseBody
    public Room createRoom(@RequestBody Room r) {
        if (r.getStatus() == null) r.setStatus("AVAILABLE");
        return roomRepository.save(r);
    }

    @PutMapping("/api/rooms/{id}")
    @ResponseBody
    public Room updateRoom(@PathVariable Long id, @RequestBody Room r) {
        return roomRepository.findById(id).map(existing -> {
            existing.setRoomNumber(r.getRoomNumber());
            existing.setRoomType(r.getRoomType());
            existing.setPrice(r.getPrice());
            existing.setStatus(r.getStatus());
            existing.setCapacity(r.getCapacity());
            return roomRepository.save(existing);
        }).orElseGet(() -> roomRepository.save(r));
    }

    @DeleteMapping("/api/rooms/{id}")
    @ResponseBody
    public Map<String, Object> deleteRoom(@PathVariable Long id) {
        long bookingCount = bookingRepository.countByRoomId(id);
        if (bookingCount > 0) {
            return Map.of("status", "error", "message", "Cannot delete room with existing bookings");
        }
        roomRepository.deleteById(id);
        return Map.of("status", "deleted");
    }

    // Bookings
    @GetMapping("/api/bookings")
    @ResponseBody
    public List<Booking> listBookings() {
        return bookingRepository.findAll();
    }

    @PostMapping("/api/bookings")
    @ResponseBody
    public Booking createBooking(@RequestBody Map<String, Object> body) {
        Booking b = new Booking();
        b.setCustomerName((String) body.getOrDefault("customerName", "Guest"));
        if (body.containsKey("roomId")) {
            Long roomId = Long.valueOf(String.valueOf(body.get("roomId")));
            roomRepository.findById(roomId).ifPresent(b::setRoom);
        }
        if (body.containsKey("checkinDate")) {
            b.setCheckinDate(LocalDate.parse((String) body.get("checkinDate")));
        }
        if (body.containsKey("checkoutDate")) {
            b.setCheckoutDate(LocalDate.parse((String) body.get("checkoutDate")));
        }
        b.setBookingStatus((String) body.getOrDefault("bookingStatus", "CONFIRMED"));
        if (body.containsKey("totalAmount")) {
            b.setTotalAmount(Double.valueOf(String.valueOf(body.get("totalAmount"))));
        } else b.setTotalAmount(0.0);
        b.setQrToken(UUID.randomUUID().toString());
        b.setGuestPass(generateGuestPass());
        Booking saved = bookingRepository.save(b);
        if (b.getRoom() != null && "CHECKED_IN".equals(b.getBookingStatus())) {
            Room room = b.getRoom();
            room.setStatus("OCCUPIED");
            roomRepository.save(room);
        }
        return saved;
    }

    @PutMapping("/api/bookings/{id}")
    @ResponseBody
    public Booking updateBooking(@PathVariable Long id, @RequestBody Booking b) {
        return bookingRepository.findById(id).map(existing -> {
            String oldStatus = existing.getBookingStatus();
            existing.setCustomerName(b.getCustomerName());
            existing.setCheckinDate(b.getCheckinDate());
            existing.setCheckoutDate(b.getCheckoutDate());
            existing.setBookingStatus(b.getBookingStatus());
            existing.setTotalAmount(b.getTotalAmount());
            if (b.getRoom() != null && !existing.getRoom().equals(b.getRoom())) {
                existing.setRoom(b.getRoom());
            }
            Booking saved = bookingRepository.save(existing);
            if (existing.getRoom() != null) {
                Room room = existing.getRoom();
                if ("CHECKED_IN".equals(b.getBookingStatus()) && !"CHECKED_IN".equals(oldStatus)) {
                    room.setStatus("OCCUPIED");
                    roomRepository.save(room);
                } else if ("CHECKED_OUT".equals(b.getBookingStatus()) && !"CHECKED_OUT".equals(oldStatus)) {
                    room.setStatus("AVAILABLE");
                    roomRepository.save(room);
                } else if ("CANCELLED".equals(b.getBookingStatus()) && !"CANCELLED".equals(oldStatus)) {
                    if (!"OCCUPIED".equals(room.getStatus())) {
                        room.setStatus("AVAILABLE");
                        roomRepository.save(room);
                    }
                }
            }
            return saved;
        }).orElseThrow(() -> new RuntimeException("Booking not found"));
    }

    @DeleteMapping("/api/bookings/{id}")
    @ResponseBody
    public Map<String, String> deleteBooking(@PathVariable Long id) {
        bookingRepository.findById(id).ifPresent(booking -> {
            if (booking.getRoom() != null) {
                Room room = booking.getRoom();
                if ("OCCUPIED".equals(room.getStatus()) || "BOOKED".equals(room.getStatus())) {
                    room.setStatus("AVAILABLE");
                    roomRepository.save(room);
                }
            }
            bookingRepository.deleteById(id);
        });
        return Map.of("status", "deleted");
    }

    // Restaurant orders
    @GetMapping("/api/orders")
    @ResponseBody
    public List<RestaurantOrder> listOrders() { return orderRepository.findAll(); }

    @PostMapping("/api/orders")
    @ResponseBody
    public RestaurantOrder createOrder(@RequestBody Map<String, Object> body) {
        RestaurantOrder o = new RestaurantOrder();
        if (body.containsKey("bookingId")) {
            Long bookingId = Long.valueOf(String.valueOf(body.get("bookingId")));
            bookingRepository.findById(bookingId).ifPresent(o::setBooking);
        }
        o.setFoodItem((String) body.getOrDefault("foodItem", "Item"));
        o.setFoodCategory((String) body.getOrDefault("foodCategory", "SNACKS"));
        o.setQuantity(Integer.valueOf(String.valueOf(body.getOrDefault("quantity", 1))));
        o.setAmount(Double.valueOf(String.valueOf(body.getOrDefault("amount", 0.0))));
        o.setOrderStatus((String) body.getOrDefault("orderStatus", "PENDING"));
        return orderRepository.save(o);
    }

    @PostMapping("/guest/{token}/order-food")
    @ResponseBody
    public Map<String, Object> guestOrderFood(@PathVariable String token, @RequestBody Map<String, Object> body) {
        Booking booking = bookingRepository.findByQrToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));
        Object itemsObj = body.get("items");
        if (!(itemsObj instanceof List<?> items)) {
            return Map.of("status", "error", "message", "No items provided");
        }
        int created = 0;
        for (Object itemObj : items) {
            if (!(itemObj instanceof Map<?, ?> item)) continue;
            Long itemId = Long.valueOf(String.valueOf(item.get("itemId")));
            Object qtyObj = item.get("qty");
            int qty = qtyObj == null ? 1 : Integer.parseInt(String.valueOf(qtyObj));
            FoodItem foodItem = foodItemRepository.findById(itemId).orElse(null);
            if (foodItem == null) continue;
            RestaurantOrder order = new RestaurantOrder();
            order.setBooking(booking);
            order.setFoodItem(foodItem.getName());
            order.setFoodCategory(foodItem.getCategory());
            order.setQuantity(qty);
            order.setAmount(foodItem.getPrice());
            order.setOrderStatus("PENDING");
            orderRepository.save(order);
            created++;
        }
        return Map.of("status", "ok", "created", created);
    }

    @PostMapping("/api/menu-items")
    @ResponseBody
    public FoodItem createMenuItem(@RequestBody Map<String, Object> body) {
        FoodItem item = new FoodItem();
        item.setName((String) body.getOrDefault("name", "Unnamed Item"));
        item.setCategory((String) body.getOrDefault("category", "OTHER"));
        Object priceObj = body.get("price");
        if (priceObj != null) {
            item.setPrice(Double.valueOf(String.valueOf(priceObj)));
        } else {
            item.setPrice(0.0);
        }
        Object availObj = body.get("available");
        if (availObj != null) {
            item.setAvailable(Boolean.valueOf(String.valueOf(availObj)));
        } else {
            item.setAvailable(true);
        }
        return foodItemRepository.save(item);
    }

    @GetMapping("/api/menu-items")
    @ResponseBody
    public List<FoodItem> listMenuItems() {
        return foodItemRepository.findAll();
    }

    @PutMapping("/api/menu-items/{id}")
    @ResponseBody
    public FoodItem updateMenuItem(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return foodItemRepository.findById(id).map(existing -> {
            if (body.containsKey("name")) existing.setName((String) body.get("name"));
            if (body.containsKey("category")) existing.setCategory((String) body.get("category"));
            if (body.containsKey("price")) existing.setPrice(Double.valueOf(String.valueOf(body.get("price"))));
            if (body.containsKey("available")) existing.setAvailable(Boolean.valueOf(String.valueOf(body.get("available"))));
            return foodItemRepository.save(existing);
        }).orElseThrow(() -> new RuntimeException("Menu item not found"));
    }

    @DeleteMapping("/api/menu-items/{id}")
    @ResponseBody
    public Map<String, String> deleteMenuItem(@PathVariable Long id) {
        foodItemRepository.deleteById(id);
        return Map.of("status", "deleted");
    }

    @PutMapping("/api/orders/{id}/status")
    @ResponseBody
    public Map<String, Object> updateOrderStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return orderRepository.findById(id).map(order -> {
            String newStatus = body.getOrDefault("status", "PENDING");
            order.setOrderStatus(newStatus);
            orderRepository.save(order);
            return Map.<String, Object>of("status", "ok", "orderId", id, "newStatus", newStatus);
        }).orElse(Map.of("status", "error", "message", "Order not found"));
    }

    @DeleteMapping("/api/orders/{id}")
    @ResponseBody
    public Map<String, String> deleteOrder(@PathVariable Long id) {
        orderRepository.deleteById(id);
        return Map.of("status", "deleted");
    }

    @PostMapping("/api/staff")
    @ResponseBody
    public Staff createStaff(@RequestBody Staff staff) {
        return staffRepository.save(staff);
    }

    @GetMapping("/api/staff")
    @ResponseBody
    public List<Staff> listStaff() {
        return staffRepository.findAll();
    }

    // Admin AI page
    @GetMapping({"/admin/ai-insights", "/ai-insights.html"})
    public String aiPage(Model model) {
        model.addAttribute("chatEndpoint", "/admin/ai-insights/chat");
        return "ai-insights";
    }

    @PostMapping("/admin/ai-insights/chat")
    @ResponseBody
    public Map<String, String> adminChat(@RequestBody Map<String, String> body) throws Exception {
        long totalBookings = bookingRepository.count();
        long activeBookings = bookingRepository.countByBookingStatus("CHECKED_IN");
        long availableRooms = roomRepository.countByStatus("AVAILABLE");
        long pendingOrders = orderRepository.countByOrderStatus("PENDING");

        String systemPrompt = String.format(
                "You are SmartStay AI, an intelligent assistant for hotel management.\n" +
                        "You have access to the following current hotel data:\n" +
                        "- Total bookings: %d\n" +
                        "- Currently checked-in guests: %d\n" +
                        "- Available rooms: %d\n" +
                        "- Pending restaurant orders: %d\n" +
                        "Answer the manager's question concisely and helpfully.\n" +
                        "Use bullet points where appropriate. Keep answers under 150 words.\n",
                totalBookings, activeBookings, availableRooms, pendingOrders
        );

        String reply = groqService.chat(systemPrompt, body.get("message"));
        return Map.of("reply", reply);
    }

    // Guest chat endpoint
    @PostMapping("/guest/{token}/ai-chat")
    @ResponseBody
    public Map<String, String> guestChat(@PathVariable String token, @RequestBody Map<String, String> body) throws Exception {
        Booking booking = bookingRepository.findByQrToken(token).orElseThrow(() -> new RuntimeException("Invalid token"));

        double roomCharges = booking.getTotalAmount() == null ? 0.0 : booking.getTotalAmount();
        double foodCharges = orderRepository.findByBookingId(booking.getId()).stream()
                .mapToDouble(o -> o.getAmount() * (o.getQuantity() == null ? 1 : o.getQuantity()))
                .sum();
        double totalWithGST = roomCharges + foodCharges; // simplified

        String systemPrompt = String.format(
                "You are a friendly AI concierge at SmartStay hotel.\n" +
                        "The guest you are speaking with is: %s\n" +
                        "Their room: %s (%s)\n" +
                        "Check-in: %s | Check-out: %s\n" +
                        "Room charges so far: ₹%.2f\n" +
                        "Food orders so far: ₹%.2f\n" +
                        "Total bill (with GST): ₹%.2f\n" +
                        "Be warm, helpful, and concise. If they ask about their bill, use the exact numbers above.\n",
                booking.getCustomerName(),
                booking.getRoom() != null ? booking.getRoom().getRoomNumber() : "-",
                booking.getRoom() != null ? booking.getRoom().getRoomType() : "-",
                booking.getCheckinDate(), booking.getCheckoutDate(), roomCharges, foodCharges, totalWithGST
        );

        String reply = groqService.chat(systemPrompt, body.get("message"));
        return Map.of("reply", reply);
    }

    // Guest portal pages (token based)
    @GetMapping({"/guest/{token}", "/guest/{token}/portal", "/guest/{token}/index.html"})
    public String guestPortal(@PathVariable String token, Model model) {
        Booking booking = bookingRepository.findByQrToken(token).orElse(null);
        model.addAttribute("token", token);
        model.addAttribute("booking", booking);
        model.addAttribute("chatEndpoint", booking != null ? "/guest/" + token + "/ai-chat" : "");
        return "guest/index";
    }

    @GetMapping({"/guest/{token}/order-food", "/guest/{token}/order-food.html"})
    public String guestOrderFood(@PathVariable String token, Model model) {
        model.addAttribute("token", token);
        model.addAttribute("menuItems", foodItemRepository.findByAvailableTrueOrderByNameAsc());
        return "guest/order-food";
    }

     @GetMapping({"/guest/{token}/my-bill", "/guest/{token}/my-bill.html"})
     public String guestBill(@PathVariable String token, Model model) {
         Booking booking = bookingRepository.findByQrToken(token).orElse(null);
         model.addAttribute("token", token);
         model.addAttribute("booking", booking);

         if (booking != null) {
             List<RestaurantOrder> orders = orderRepository.findByBookingId(booking.getId());
             model.addAttribute("orders", orders);

             // Calculate totals for template
             Double roomCharge = booking.getTotalAmount() != null ? booking.getTotalAmount() : 0.0;
             Double foodTotal = orders.stream()
                     .mapToDouble(o -> o.getAmount() * (o.getQuantity() == null ? 1 : o.getQuantity()))
                     .sum();
             Double gstOnRooms = roomCharge * 0.12;
             Double gstOnFood = foodTotal * 0.05;
             Double grandTotal = roomCharge + foodTotal + gstOnRooms + gstOnFood;

             model.addAttribute("roomCharge", roomCharge);
             model.addAttribute("foodTotal", foodTotal);
             model.addAttribute("gstOnRooms", gstOnRooms);
             model.addAttribute("gstOnFood", gstOnFood);
             model.addAttribute("grandTotal", grandTotal);
         }

         return "guest/my-bill";
     }

    @GetMapping({"/guest/{token}/my-stay", "/guest/{token}/my-stay.html"})
    public String guestStay(@PathVariable String token, Model model) {
        Booking booking = bookingRepository.findByQrToken(token).orElse(null);
        model.addAttribute("token", token);
        model.addAttribute("booking", booking);
        model.addAttribute("chatEndpoint", booking != null ? "/guest/" + token + "/ai-chat" : "");

        double roomCharges = booking != null && booking.getTotalAmount() != null ? booking.getTotalAmount() : 0.0;
        List<RestaurantOrder> orders = booking != null ? orderRepository.findByBookingId(booking.getId()) : List.of();
        double foodCharges = orders.stream().mapToDouble(o -> o.getAmount() * (o.getQuantity() == null ? 1 : o.getQuantity())).sum();

        model.addAttribute("roomCharges", roomCharges);
        model.addAttribute("foodCharges", foodCharges);
        model.addAttribute("totalOrders", orders.size());
        model.addAttribute("totalSpend", roomCharges + foodCharges);

        int totalDays = 0;
        int daysStayed = 0;
        int daysRemaining = 0;
        if (booking != null && booking.getCheckinDate() != null && booking.getCheckoutDate() != null) {
            totalDays = (int) java.time.temporal.ChronoUnit.DAYS.between(booking.getCheckinDate(), booking.getCheckoutDate());
            daysStayed = (int) java.time.temporal.ChronoUnit.DAYS.between(booking.getCheckinDate(), java.time.LocalDate.now());
            daysRemaining = Math.max(totalDays - daysStayed, 0);
        }
        double progress = totalDays > 0 ? (daysStayed * 100.0 / totalDays) : 0.0;
        model.addAttribute("stayProgressPercent", Math.min(Math.max(progress, 0.0), 100.0));
        model.addAttribute("stayDayLabel", totalDays > 0 ? "Day " + Math.min(daysStayed + 1, totalDays) + " of " + totalDays : "Day 0 of 0");
        model.addAttribute("nightsRemaining", daysRemaining);

        long hoursToCheckout = 0;
        if (booking != null && booking.getCheckoutDate() != null) {
            hoursToCheckout = java.time.Duration.between(java.time.LocalDateTime.now(), booking.getCheckoutDate().atStartOfDay()).toHours();
        }
        model.addAttribute("hoursToCheckout", Math.max(hoursToCheckout, 0));
        model.addAttribute("avgRating", 0.0);

        Map<String, Long> ordersByDay = new LinkedHashMap<>();
        for (RestaurantOrder o : orders) {
            String key = o.getOrderedAt() == null ? "" : o.getOrderedAt().toLocalDate().toString();
            ordersByDay.put(key, ordersByDay.getOrDefault(key, 0L) + 1);
        }
        model.addAttribute("orderDayLabels", new ArrayList<>(ordersByDay.keySet()));
        model.addAttribute("orderDayCounts", ordersByDay.values().stream().map(Long::doubleValue).collect(Collectors.toList()));
        return "guest/my-stay";
    }

    @GetMapping({"/guest/{token}/feedback", "/guest/{token}/feedback.html"})
    public String guestFeedback(@PathVariable String token, Model model) {
        model.addAttribute("token", token);
        return "guest/feedback";
    }

    // Internal helper to seed admin user if missing (temporary endpoint)
    @PostMapping("/internal/seed-admin")
    @ResponseBody
    public Map<String, String> seedAdmin() {
        try {
            if (userRepository.findByEmail("admin@smartstay.com").isEmpty()) {
                User admin = new User();
                admin.setName("Admin Kumar");
                admin.setEmail("admin@smartstay.com");
                admin.setPassword(passwordEncoder.encode("Admin@123"));
                admin.setRole("ADMIN");
                userRepository.save(admin);
                return Map.of("status", "created");
            } else {
                return Map.of("status", "exists");
            }
        } catch (Exception e) {
            return Map.of("status", "error", "message", e.getMessage());
        }
    }

    @GetMapping("/internal/users")
    @ResponseBody
    public List<String> listUsers() {
        return userRepository.findAll().stream().map(User::getEmail).collect(Collectors.toList());
    }

    // Reports - sample endpoint returning JSON for daily revenue last 30 days
    @GetMapping("/admin/reports/data/daily-revenue")
    @ResponseBody
    public Map<String, Object> dailyRevenue() {
        LocalDateTime from = LocalDateTime.now().minusDays(30);
        List<Object[]> rows = bookingRepository.getDailyRevenueSince(from);
        List<String> dates = rows.stream().map(r -> String.valueOf(r[0])).collect(Collectors.toList());
        List<Double> amounts = rows.stream().map(r -> toDouble(r[1])).collect(Collectors.toList());
        return Map.of("dates", dates, "amounts", amounts);
    }

    private String generateGuestPass() {
        int value = new java.util.Random().nextInt(900000) + 100000;
        return String.valueOf(value);
    }

    private double toDouble(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Number number) return number.doubleValue();
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return 0.0;
        }
    }

    private int toInt(Object value) {
        if (value == null) return 0;
        if (value instanceof Number number) return number.intValue();
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }
}
