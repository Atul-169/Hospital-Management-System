package com.example.project.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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

            // কুয়েরিগুলো এক্সিকিউট করা
            stmt.execute(createUsersTable);
            stmt.execute(createDoctorsTable);
            stmt.execute(createPatientsTable);

            // ১০. কলাম চেক এবং অ্যাড (যদি টেবিল আগে থেকেই থাকে কিন্তু কলাম না থাকে)
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

            // ৪. অ্যাপয়েন্টমেন্ট টেবিল
            String createAppointmentsTable = "CREATE TABLE IF NOT EXISTS appointments (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "patient_id INTEGER," +
                    "doctor_id INTEGER," +
                    "appointment_date TEXT," +
                    "appointment_time TEXT," +
                    "visit_reason TEXT," +
                    "status TEXT DEFAULT 'upcoming'," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY(patient_id) REFERENCES patients(id)," +
                    "FOREIGN KEY(doctor_id) REFERENCES doctors(id)" +
                    ");";

            // ৫. মেডিকেল রিপোর্ট টেবিল
            String createReportsTable = "CREATE TABLE IF NOT EXISTS medical_reports (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "patient_id INTEGER," +
                    "doctor_id INTEGER," +
                    "appointment_id INTEGER," +
                    "diagnosis TEXT," +
                    "prescription TEXT," +
                    "doctor_notes TEXT," +
                    "report_date TEXT," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY(patient_id) REFERENCES patients(id)," +
                    "FOREIGN KEY(doctor_id) REFERENCES doctors(id)," +
                    "FOREIGN KEY(appointment_id) REFERENCES appointments(id)" +
                    ");";

            // ৬. নোটিফিকেশন টেবিল
            String createNotificationsTable = "CREATE TABLE IF NOT EXISTS notifications (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id INTEGER," +
                    "title TEXT," +
                    "message TEXT," +
                    "is_read INTEGER DEFAULT 0," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY(user_id) REFERENCES users(id)" +
                    ");";

            // ৭. ব্লাড ডোনার টেবিল
            String createBloodDonorsTable = "CREATE TABLE IF NOT EXISTS blood_donors (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "patient_id INTEGER UNIQUE," +
                    "blood_group TEXT," +
                    "location TEXT," +
                    "phone TEXT," +
                    "availability_status TEXT DEFAULT 'available'," +
                    "last_donation_date TEXT," +
                    "FOREIGN KEY(patient_id) REFERENCES patients(id)" +
                    ");";

            // ৮. ডক্টর পোস্ট টেবিল
            String createDoctorPostsTable = "CREATE TABLE IF NOT EXISTS doctor_posts (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "doctor_id INTEGER," +
                    "title TEXT," +
                    "content TEXT," +
                    "category TEXT," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY(doctor_id) REFERENCES doctors(id)" +
                    ");";

            // ৯. Q&A টেবিল
            String createQuestionsTable = "CREATE TABLE IF NOT EXISTS questions (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "patient_id INTEGER," +
                    "question TEXT," +
                    "answer TEXT," +
                    "answered_by INTEGER," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "answered_at TIMESTAMP," +
                    "FOREIGN KEY(patient_id) REFERENCES patients(id)," +
                    "FOREIGN KEY(answered_by) REFERENCES doctors(id)" +
                    ");";

            stmt.execute(createAppointmentsTable);
            stmt.execute(createReportsTable);
            stmt.execute(createNotificationsTable);
            stmt.execute(createBloodDonorsTable);
            stmt.execute(createDoctorPostsTable);
            stmt.execute(createQuestionsTable);

            normalizeUserRoles(conn);
            ensureProfileRows(conn, "Doctor");
            ensureProfileRows(conn, "Patient");

            System.out.println("[DEBUG_LOG] Database Tables Initialized Successfully!");

            // Add sample doctors if none exist
            insertSampleDoctors(conn);

        } catch (Exception e) {
            System.err.println("[DEBUG_LOG] Database Initialization Failed: " + e.getMessage());
        }
    }

    /**
     * Insert sample doctors into the database for testing
     */
    private static void insertSampleDoctors(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            // Check if doctors already exist
            var rs = stmt.executeQuery("SELECT COUNT(*) as count FROM users WHERE LOWER(role) = 'doctor'");
            if (rs.next() && rs.getInt("count") > 0) {
                System.out.println("[DEBUG_LOG] Sample doctors already exist");
                return;
            }

            // Sample doctor data
            String[][] doctors = {
                {"Dr. Sarah Ahmed", "sarah.ahmed@hospital.com", "doctor123", "Cardiologist", "15 years", "MBBS, MD (Cardiology)", "01712345671", "2500"},
                {"Dr. Mohammad Rahman", "mohammad.rahman@hospital.com", "doctor123", "Neurologist", "12 years", "MBBS, FCPS (Neurology)", "01712345672", "2200"},
                {"Dr. Fatima Khan", "fatima.khan@hospital.com", "doctor123", "Pediatrician", "10 years", "MBBS, DCH, MD (Pediatrics)", "01712345673", "1800"},
                {"Dr. Kamal Hossain", "kamal.hossain@hospital.com", "doctor123", "Orthopedic Surgeon", "18 years", "MBBS, MS (Orthopedics)", "01712345674", "2800"},
                {"Dr. Nusrat Jahan", "nusrat.jahan@hospital.com", "doctor123", "Dermatologist", "8 years", "MBBS, DDV, MD (Dermatology)", "01712345675", "1500"},
                {"Dr. Rashid Ali", "rashid.ali@hospital.com", "doctor123", "General Physician", "20 years", "MBBS, FCPS (Medicine)", "01712345676", "1200"},
                {"Dr. Ayesha Begum", "ayesha.begum@hospital.com", "doctor123", "Gynecologist", "14 years", "MBBS, FCPS (Gynecology)", "01712345677", "2000"},
                {"Dr. Tariq Mahmud", "tariq.mahmud@hospital.com", "doctor123", "ENT Specialist", "11 years", "MBBS, FCPS (ENT)", "01712345678", "1800"},
                {"Dr. Shabnam Ara", "shabnam.ara@hospital.com", "doctor123", "Psychiatrist", "9 years", "MBBS, MPhil (Psychiatry)", "01712345679", "1600"},
                {"Dr. Imran Sheikh", "imran.sheikh@hospital.com", "doctor123", "Gastroenterologist", "13 years", "MBBS, FCPS (Gastroenterology)", "01712345680", "2100"}
            };

            for (String[] doctor : doctors) {
                // Insert user
                String insertUser = String.format(
                    "INSERT INTO users (name, email, password, role) VALUES ('%s', '%s', '%s', 'Doctor')",
                    doctor[0], doctor[1], doctor[2]
                );
                stmt.execute(insertUser);

                // Get the inserted user ID
                var userRs = stmt.executeQuery("SELECT last_insert_rowid() as id");
                int userId = userRs.getInt("id");

                // Insert doctor profile
                String insertDoctor = String.format(
                    "INSERT INTO doctors (user_id, specialization, experience, qualification, phone, fee, profile_completed) " +
                    "VALUES (%d, '%s', '%s', '%s', '%s', %s, 1)",
                    userId, doctor[3], doctor[4], doctor[5], doctor[6], doctor[7]
                );
                stmt.execute(insertDoctor);
            }

            System.out.println("[DEBUG_LOG] Sample doctors inserted successfully!");

        } catch (Exception e) {
            System.err.println("[DEBUG_LOG] Failed to insert sample doctors: " + e.getMessage());
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

    private static void normalizeUserRoles(Connection conn) {
        String sql = "UPDATE users SET role = CASE " +
                "WHEN LOWER(TRIM(role)) = 'admin' THEN 'Admin' " +
                "WHEN LOWER(TRIM(role)) = 'doctor' THEN 'Doctor' " +
                "WHEN LOWER(TRIM(role)) = 'patient' THEN 'Patient' " +
                "ELSE role END";

        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (Exception e) {
            System.err.println("[DEBUG_LOG] Failed to normalize roles: " + e.getMessage());
        }
    }

    private static void ensureProfileRows(Connection conn, String role) {
        String userQuery = "SELECT id FROM users WHERE role = ?";
        String profileQuery = "SELECT 1 FROM " + role.toLowerCase() + "s WHERE user_id = ?";
        String insertQuery = "INSERT INTO " + role.toLowerCase() + "s (user_id, profile_completed) VALUES (?, 0)";

        try (PreparedStatement userStmt = conn.prepareStatement(userQuery);
             PreparedStatement profileStmt = conn.prepareStatement(profileQuery);
             PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {

            userStmt.setString(1, role);
            try (ResultSet userRs = userStmt.executeQuery()) {
                while (userRs.next()) {
                    int userId = userRs.getInt("id");
                    profileStmt.setInt(1, userId);
                    try (ResultSet profileRs = profileStmt.executeQuery()) {
                        if (!profileRs.next()) {
                            insertStmt.setInt(1, userId);
                            insertStmt.executeUpdate();
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[DEBUG_LOG] Failed to ensure " + role + " profile rows: " + e.getMessage());
        }
    }
}
