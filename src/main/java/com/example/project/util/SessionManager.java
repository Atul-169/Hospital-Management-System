package com.example.project.util;

public class SessionManager {
    private static String selectedRole; // "Admin", "Doctor", or "Patient"
    private static String userName;
    private static int userId;

    public static String normalizeRole(String role) {
        if (role == null) {
            return null;
        }

        String normalized = role.trim().toLowerCase();
        return switch (normalized) {
            case "admin" -> "Admin";
            case "doctor" -> "Doctor";
            case "patient" -> "Patient";
            default -> role.trim();
        };
    }

    public static String getSelectedRole() {
        return selectedRole;
    }

    public static void setSelectedRole(String role) {
        selectedRole = normalizeRole(role);
        System.out.println("[DEBUG_LOG] Role set in SessionManager: " + role);
    }

    public static String getUserName() {
        return userName;
    }

    public static void setUserName(String name) {
        userName = name;
    }

    public static int getUserId() {
        return userId;
    }

    public static void setUserId(int id) {
        userId = id;
    }
    
    public static void clearSession() {
        selectedRole = null;
        userName = null;
        userId = 0;
    }
}
