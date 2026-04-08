package com.example.project.controller;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
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
import javafx.scene.layout.HBox;
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
import javafx.concurrent.Task;


public class LoginController {


    @FXML
    private Label roleLabel;


    @FXML
    private Label errorLabel;


    @FXML
    private HBox forgotPasswordBox;


    @FXML
    private Label forgotPasswordLabel;


    @FXML
    private HBox registerBox;


    @FXML
    private Label registerNowLabel;


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
        boolean isAdmin = "admin".equalsIgnoreCase(role);
        if (forgotPasswordBox != null) {
            forgotPasswordBox.setVisible(!isAdmin);
            forgotPasswordBox.setManaged(!isAdmin);
        }
        if (registerBox != null) {
            registerBox.setVisible(!isAdmin);
            registerBox.setManaged(!isAdmin);
        }
        installHoverAnimation(loginBtn);
        Platform.runLater(() -> {
            usernameField.getScene().setOnKeyPressed(event -> {
                switch (event.getCode()) {
                    case ENTER -> loginBtn.fire();
                    case ESCAPE -> handleBack(new ActionEvent(loginBtn, null));
                }
            });
        });
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
        String loginIdentifier = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText().trim();
        String selectedRole = SessionManager.normalizeRole(SessionManager.getSelectedRole());

        if (selectedRole == null || selectedRole.isBlank()) {
            showError("Please select a role first.");
            return;
        }

        if (loginIdentifier.isEmpty() || password.isEmpty()) {
            showError("Please fill all fields!");
            return;
        }

        loginBtn.setDisable(true);
        loginBtn.setText("Connecting...");
        errorLabel.setVisible(false);

        Task<String[]> loginTask = new Task<>() {
            @Override
            protected String[] call() throws Exception {
                String sql = "SELECT * FROM users " +
                        "WHERE (LOWER(TRIM(email)) = LOWER(?) OR LOWER(TRIM(name)) = LOWER(?)) " +
                        "AND password = ? " +
                        "AND LOWER(TRIM(role)) = LOWER(?)";
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {

                    pstmt.setString(1, loginIdentifier);
                    pstmt.setString(2, loginIdentifier);
                    pstmt.setString(3, password);
                    pstmt.setString(4, selectedRole);

                    ResultSet rs = pstmt.executeQuery();
                    if (rs.next()) {
                        return new String[]{
                                rs.getString("name"),
                                String.valueOf(rs.getInt("id")),
                                rs.getString("role")
                        };
                    }
                }
                return null; // login failed
            }

        };

        loginTask.setOnSucceeded(e -> {
            String[] result = loginTask.getValue();

            loginBtn.setDisable(false);
            loginBtn.setText("Sign In");

            if (result == null) {
                showError("Invalid email, password or role!");
                return;
            }

            String name = result[0];
            int id = Integer.parseInt(result[1]);
            String userRole = SessionManager.normalizeRole(result[2]);

            SessionManager.setUserName(name);
            SessionManager.setUserId(id);
            SessionManager.setSelectedRole(userRole);

            if (userRole.equalsIgnoreCase("Patient")) {
                checkPatientProfile(event, id);
            } else if (userRole.equalsIgnoreCase("Doctor")) {
                checkDoctorProfile(event, id);
            } else {
                loadScene(event, "/fxml/" + userRole.toLowerCase() + "-dashboard.fxml");
            }

        });

        loginTask.setOnFailed(e -> {
            loginBtn.setDisable(false);
            loginBtn.setText("Sign In");
            showError("Database error: " + loginTask.getException().getMessage());
        });

        new Thread(loginTask).start();
    }

    private void checkDoctorProfile(ActionEvent event, int userId) {
        ensureProfileRowExists("doctors", userId);
        loadScene(event, "/fxml/doctor-dashboard.fxml");
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
            Scene scene = SceneManager.createScene(loader.load(), fxmlPath);
            Stage stage;
            if (eventSource instanceof ActionEvent) {
                stage = (Stage) ((Node) ((ActionEvent) eventSource).getSource()).getScene().getWindow();
            } else if (eventSource instanceof MouseEvent) {
                stage = (Stage) ((Node) ((MouseEvent) eventSource).getSource()).getScene().getWindow();
            } else {
                stage = (Stage) ((Node) eventSource).getScene().getWindow();
            }
            stage.setScene(scene);
            SceneManager.configureStage(stage, fxmlPath);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
