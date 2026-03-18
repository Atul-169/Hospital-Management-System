package com.example.project.util;

import java.sql.Connection;
import java.sql.Statement;

public class DBInitializer {

    /**
     * এই মেথডটি অ্যাপ রান করলে একবার কল হবে এবং প্রয়োজনীয় টেবিলগুলো তৈরি করবে।
     */
    public static void initialize() {
        // DatabaseConnection ক্লাস ব্যবহার করে কানেকশন নেওয়া
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            if (conn == null) return;

            // ১. ইউজার টেবিল তৈরি করার SQL কুয়েরি
            String createUsersTable = "CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT NOT NULL," +
                    "email TEXT UNIQUE NOT NULL," +
                    "password TEXT NOT NULL," +
                    "role TEXT NOT NULL" +
                    ");";

            // ২. ডক্টর টেবিল
            String createDoctorsTable = "CREATE TABLE IF NOT EXISTS doctors (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id INTEGER UNIQUE," +
                    "specialization TEXT," +
                    "experience TEXT," +
                    "qualification TEXT," +
                    "phone TEXT," +
                    "fee REAL," +
                    "profile_completed INTEGER DEFAULT 0," +
                    "FOREIGN KEY(user_id) REFERENCES users(id)" +
                    ");";

            // ৩. পেশেন্ট টেবিল (ইউজার টেবিলের সাথে রিলেশন রাখা যাবে পরে)
            String createPatientsTable = "CREATE TABLE IF NOT EXISTS patients (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id INTEGER UNIQUE," +
                    "phone TEXT," +
                    "blood_group TEXT," +
                    "address TEXT," +
                    "allergies TEXT," +
                    "diabetes TEXT," +
                    "medical_history TEXT," +
                    "profile_completed INTEGER DEFAULT 0," +
                    "FOREIGN KEY(user_id) REFERENCES users(id)" +
                    ");";

            // ৪. কলাম চেক এবং অ্যাড (যদি টেবিল আগে থেকেই থাকে কিন্তু কলাম না থাকে)
            // Doctors Table columns
            addColumnIfNotExists(stmt, "doctors", "experience", "TEXT");
            addColumnIfNotExists(stmt, "doctors", "qualification", "TEXT");
            addColumnIfNotExists(stmt, "doctors", "phone", "TEXT");
            addColumnIfNotExists(stmt, "doctors", "profile_completed", "INTEGER DEFAULT 0");

            // Patients Table columns
            addColumnIfNotExists(stmt, "patients", "phone", "TEXT");
            addColumnIfNotExists(stmt, "patients", "allergies", "TEXT");
            addColumnIfNotExists(stmt, "patients", "diabetes", "TEXT");
            addColumnIfNotExists(stmt, "patients", "profile_completed", "INTEGER DEFAULT 0");

            // কুয়েরিগুলো এক্সিকিউট করা
            stmt.execute(createUsersTable);
            stmt.execute(createDoctorsTable);
            stmt.execute(createPatientsTable);

            System.out.println("[DEBUG_LOG] Database Tables Initialized Successfully!");

        } catch (Exception e) {
            System.err.println("[DEBUG_LOG] Database Initialization Failed: " + e.getMessage());
        }
    }

    /**
     * SQLite এ যদি কলাম না থাকে তবে সেটি অ্যাড করার জন্য হেল্পার মেথড।
     */
    private static void addColumnIfNotExists(Statement stmt, String tableName, String columnName, String columnType) {
        try {
            stmt.execute("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + columnType);
            System.out.println("[DEBUG_LOG] Column '" + columnName + "' added to '" + tableName + "'");
        } catch (Exception e) {
            // যদি কলাম অলরেডি থাকে, তবে SQLite এরর দেবে, আমরা সেটি ইগনোর করবো।
        }
    }
}
