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
import java.sql.Statement;
import com.example.project.util.DatabaseConnection;
import com.example.project.util.SceneManager;
import com.example.project.util.SessionManager;

public class RegisterController {

    @FXML
    private Label roleLabel;

    @FXML
    private Label errorLabel;

    @FXML
    private TextField nameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Button registerBtn;

    @FXML
    public void initialize() {
        String role = SessionManager.getSelectedRole();
        if (role != null) {
            roleLabel.setText(role.toUpperCase() + " REGISTRATION");
        }
        installHoverAnimation(registerBtn);
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
    public void handleRegister(ActionEvent event) {
        String name = nameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String role = SessionManager.normalizeRole(SessionManager.getSelectedRole());

        // ১. বেসিক ভ্যালিডেশন
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showError("Please fill all fields!");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match!");
            return;
        }

        // ২. ডাটাবেজে ডাটা ইনসার্ট করা
        // SQL Injection থেকে বাঁচার জন্য PreparedStatement ব্যবহার করা হয়
        String sql = "INSERT INTO users (name, email, password, role) VALUES (?, ?, ?, ?)";

        try (
                Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // '?' চিহ্নের জায়গায় ভ্যালু সেট করা
            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.setString(3, password);
            pstmt.setString(4, role);

            // কুয়েরি রান করা
            pstmt.executeUpdate();

            // ৪. ডক্টর বা পেশেন্ট টেবিলে প্রাথমিক এন্ট্রি করা (যাতে লগইনের সময় চেক করা যায়)
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int userId = rs.getInt(1);
                    if (role.equalsIgnoreCase("Doctor")) {
                        String doctorSql = "INSERT INTO doctors (user_id, profile_completed) VALUES (?, 0)";
                        try (PreparedStatement dpstmt = conn.prepareStatement(doctorSql)) {
                            dpstmt.setInt(1, userId);
                            dpstmt.executeUpdate();
                        }
                    } else if (role.equalsIgnoreCase("Patient")) {
                        String patientSql = "INSERT INTO patients (user_id, profile_completed) VALUES (?, 0)";
                        try (PreparedStatement ppstmt = conn.prepareStatement(patientSql)) {
                            ppstmt.setInt(1, userId);
                            ppstmt.executeUpdate();
                        }
                    }
                }
            }

            System.out.println("[DEBUG_LOG] User Registered Successfully!");

            // ৩. রেজিস্ট্রেশন সাকসেস হলে মেসেজ দেখানো
            showSuccess("Registration Successful! Please Sign In.");

            // Clear fields
            nameField.clear();
            emailField.clear();
            passwordField.clear();
            confirmPasswordField.clear();

        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                showError("Email already exists!");
            } else {
                showError("Registration Failed: " + e.getMessage());
            }
        }
    }

    private void showSuccess(String message) {
        errorLabel.setText(message);
        errorLabel.getStyleClass().remove("error-label");
        errorLabel.getStyleClass().add("success-label");
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), errorLabel);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.getStyleClass().remove("success-label");
        errorLabel.getStyleClass().add("error-label");
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
    public void handleBackToLogin(ActionEvent event) {
        loadScene(event, "/fxml/login.fxml");
    }

    @FXML
    public void handleBackToLoginMouse(MouseEvent event) {
        loadScene(event, "/fxml/login.fxml");
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
