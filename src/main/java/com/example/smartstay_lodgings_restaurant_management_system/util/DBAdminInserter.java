package com.example.smartstay_lodgings_restaurant_management_system.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DBAdminInserter {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/smartstay";
        String user = "postgres";
        String pass = "1234";
        String email = "admin@smartstay.com";
        try {
            Class.forName("org.postgresql.Driver");
            try (Connection c = DriverManager.getConnection(url, user, pass)) {
                String check = "SELECT COUNT(*) FROM users WHERE email = ?";
                try (PreparedStatement ps = c.prepareStatement(check)) {
                    ps.setString(1, email);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            System.out.println("User already exists: " + email);
                            return;
                        }
                    }
                }
                String bcryptHash = "$2a$10$eD3hepq9XWwvf3GBnEYMFe8i0DqvVJVJL.fZ15EEjRfKBDwH.QcH.";
                String insert = "INSERT INTO users (name, email, password, role) VALUES (?, ?, ?, ?)";
                try (PreparedStatement ps = c.prepareStatement(insert)) {
                    ps.setString(1, "Admin Kumar");
                    ps.setString(2, email);
                    ps.setString(3, bcryptHash);
                    ps.setString(4, "ADMIN");
                    int r = ps.executeUpdate();
                    System.out.println("Inserted admin user, rows=" + r);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(2);
        }
    }
}

