package com.example.project.controller;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.example.project.util.DatabaseConnection;
import com.example.project.util.SceneManager;
import com.example.project.util.SessionManager;

public class LoginController {

    @FXML
    private Label roleLabel;

    @FXML
    private Label errorLabel;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginBtn;

    @FXML
    public void initialize() {
        String role = SessionManager.getSelectedRole();
        if (role != null) {
            roleLabel.setText(role.toUpperCase() + " LOGIN");
        } else {
            roleLabel.setText("SELECT ROLE FIRST");
        }
        installHoverAnimation(loginBtn);
    }

    private void installHoverAnimation(Button btn) {
        if (btn == null) return;

        DropShadow glow = new DropShadow();
        glow.setColor(Color.rgb(59, 130, 246, 0.55));
        glow.setRadius(20);

        btn.setOnMouseEntered(e -> {
            ScaleTransition s = new ScaleTransition(Duration.millis(200), btn);
            s.setToX(1.03);
            s.setToY(1.03);
            s.setInterpolator(Interpolator.EASE_BOTH);
            s.play();
            btn.setEffect(glow);
        });

            btn.setOnMouseExited(e -> {
            ScaleTransition s = new ScaleTransition(Duration.millis(200), btn);
            s.setToX(1.0);
            s.setToY(1.0);
            s.setInterpolator(Interpolator.EASE_BOTH);
            s.play();
            btn.setEffect(null);
        });
    }

    @FXML
    public void handleLogin(ActionEvent event) {
        String email = usernameField.getText(); // ইউজার ইমেইল দিয়ে লগইন করবে
        String password = passwordField.getText();
        String selectedRole = SessionManager.normalizeRole(SessionManager.getSelectedRole());

        if (selectedRole == null || selectedRole.isBlank()) {
            showError("Please select a role first.");
            return;
        }

        if (email.isEmpty() || password.isEmpty()) {
            showError("Please fill all fields!");
            return;
        }

        // ডাটাবেজ থেকে ইউজার চেক করার SQL (SELECT)
        String sql = "SELECT * FROM users WHERE email = ? AND password = ? AND LOWER(role) = LOWER(?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            pstmt.setString(2, password);
            pstmt.setString(3, selectedRole);

            // কুয়েরি রান করে রেজাল্ট সেট আনা
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // সাকসেসফুল লগইন
                String name = rs.getString("name");
                int id = rs.getInt("id");
                String userRole = SessionManager.normalizeRole(rs.getString("role")); // Database থেকে আসল রোল নেওয়া
                System.out.println("[DEBUG_LOG] Login Successful for: " + name);

                // সেশনে ডাটা রাখা
                SessionManager.setUserName(name);
                SessionManager.setUserId(id);
                SessionManager.setSelectedRole(userRole); // Session এ রোল আপডেট করা

                if (userRole.equalsIgnoreCase("Patient")) {
                    checkPatientProfile(event, id);
                } else if (userRole.equalsIgnoreCase("Doctor")) {
                    checkDoctorProfile(event, id);
                } else {
                    // রোল অনুযায়ী ড্যাশবোর্ড ওপেন করা
                    String dashboardPath = "/fxml/" + userRole.toLowerCase() + "-dashboard.fxml";
                    loadScene(event, dashboardPath);
                }
            } else {
                // ভুল তথ্য
                showError("Invalid email, password or role!");
            }

        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
        }
    }

    private void checkDoctorProfile(ActionEvent event, int userId) {
        System.out.println("[DEBUG_LOG] Checking doctor profile completion for user ID: " + userId);
        ensureProfileRowExists("doctors", userId);
        String sql = "SELECT profile_completed FROM doctors WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int status = rs.getInt("profile_completed");
                System.out.println("[DEBUG_LOG] Doctor profile_completed: " + status);
                if (status == 1) {
                    loadScene(event, "/fxml/doctor-dashboard.fxml");
                } else {
                    loadScene(event, "/fxml/profile-setup.fxml");
                }
            } else {
                System.out.println("[DEBUG_LOG] No doctor record found for user ID: " + userId);
                loadScene(event, "/fxml/profile-setup.fxml");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            loadScene(event, "/fxml/doctor-dashboard.fxml"); // Fallback
        }
    }

    private void checkPatientProfile(ActionEvent event, int userId) {
        ensureProfileRowExists("patients", userId);
        String sql = "SELECT profile_completed FROM patients WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && rs.getInt("profile_completed") == 1) {
                loadScene(event, "/fxml/patient-dashboard.fxml");
            } else {
                loadScene(event, "/fxml/profile-setup.fxml");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            loadScene(event, "/fxml/patient-dashboard.fxml"); // Fallback
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);

        // Animation: Fade in and Shake
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), errorLabel);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        TranslateTransition shake = new TranslateTransition(Duration.millis(100), errorLabel);
        shake.setFromX(0);
        shake.setByX(10);
        shake.setCycleCount(4);
        shake.setAutoReverse(true);

        new ParallelTransition(fadeIn, shake).play();
    }

    @FXML
    public void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/role-selection.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(loader.load());
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void ensureProfileRowExists(String tableName, int userId) {
        String query = "INSERT INTO " + tableName + " (user_id, profile_completed) " +
                "SELECT ?, 0 WHERE NOT EXISTS (SELECT 1 FROM " + tableName + " WHERE user_id = ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DEBUG_LOG] Failed to ensure profile row in " + tableName + ": " + e.getMessage());
        }
    }

    @FXML
    public void handleRegisterRedirect(MouseEvent event) {
        loadScene(event, "/fxml/register.fxml");
    }

    private void loadScene(Object eventSource, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Stage stage;
            if (eventSource instanceof ActionEvent) {
                stage = (Stage) ((Node) ((ActionEvent) eventSource).getSource()).getScene().getWindow();
            } else if (eventSource instanceof MouseEvent) {
                stage = (Stage) ((Node) ((MouseEvent) eventSource).getSource()).getScene().getWindow();
            } else {
                stage = (Stage) ((Node) eventSource).getScene().getWindow();
            }
            Scene scene = new Scene(loader.load());
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
