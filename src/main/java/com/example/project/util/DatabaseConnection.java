package com.example.project.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // ডাটাবেজ ফাইলের পাথ (প্রজেক্ট রুট ফোল্ডারে hospital.db নামে তৈরি হবে)
    private static final String URL = "jdbc:sqlite:hospital.db";

    /**
     * ডাটাবেজের সাথে কানেকশন তৈরি করার মেথড।
     * @return Connection অবজেক্ট
     */
    public static Connection getConnection() {
        Connection conn = null;
        try {
            // SQLite ড্রাইভার লোড করা এবং কানেকশন ওপেন করা
            conn = DriverManager.getConnection(URL);
            System.out.println("[DEBUG_LOG] Database Connected Successfully!");
        } catch (SQLException e) {
            System.err.println("[DEBUG_LOG] Database Connection Failed: " + e.getMessage());
        }
        return conn;
    }
}
