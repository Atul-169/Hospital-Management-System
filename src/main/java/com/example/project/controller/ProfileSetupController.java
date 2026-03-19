package com.example.project.controller;

import com.example.project.util.DatabaseConnection;
import com.example.project.util.SceneManager;
import com.example.project.util.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ProfileSetupController {

    @FXML private VBox patientFields;
    @FXML private VBox doctorFields;

    // Patient Fields
    @FXML private TextField phoneField;
    @FXML private ComboBox<String> bloodGroupCombo;
    @FXML private TextField addressField;
    @FXML private CheckBox allergiesCheck;
    @FXML private CheckBox diabetesCheck;
    @FXML private TextArea historyArea;

    // Doctor Fields
    @FXML private TextField specializationField;
    @FXML private TextField qualificationField;
    @FXML private TextField experienceField;
    @FXML private TextField docPhoneField;
    @FXML private TextField feeField;

    @FXML
    public void initialize() {
        String role = SessionManager.getSelectedRole();
        if ("Doctor".equalsIgnoreCase(role)) {
            patientFields.setVisible(false);
            patientFields.setManaged(false);
            doctorFields.setVisible(true);
            doctorFields.setManaged(true);
            loadDoctorData();
        } else {
            patientFields.setVisible(true);
            patientFields.setManaged(true);
            doctorFields.setVisible(false);
            doctorFields.setManaged(false);
            bloodGroupCombo.getItems().setAll("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-");
            loadPatientData();
        }
    }

    private void loadPatientData() {
        String query = "SELECT * FROM patients WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, SessionManager.getUserId());
            java.sql.ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                phoneField.setText(rs.getString("phone"));
                bloodGroupCombo.setValue(rs.getString("blood_group"));
                addressField.setText(rs.getString("address"));
                allergiesCheck.setSelected("Yes".equalsIgnoreCase(rs.getString("allergies")));
                diabetesCheck.setSelected("Yes".equalsIgnoreCase(rs.getString("diabetes")));
                historyArea.setText(rs.getString("medical_history"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadDoctorData() {
        String query = "SELECT * FROM doctors WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, SessionManager.getUserId());
            java.sql.ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                specializationField.setText(rs.getString("specialization"));
                qualificationField.setText(rs.getString("qualification"));
                experienceField.setText(rs.getString("experience"));
                docPhoneField.setText(rs.getString("phone"));
                feeField.setText(String.valueOf(rs.getDouble("fee")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML private Button saveBtn, cancelBtn;

    @FXML
    public void handleCancel(ActionEvent event) {
        String role = SessionManager.getSelectedRole();
        if ("Doctor".equalsIgnoreCase(role)) {
            loadScene(event, "/fxml/doctor-dashboard.fxml");
        } else {
            loadScene(event, "/fxml/patient-dashboard.fxml");
        }
    }

    @FXML
    public void handleSaveProfile(ActionEvent event) {
        String role = SessionManager.getSelectedRole();
        if ("Doctor".equalsIgnoreCase(role)) {
            saveDoctorProfile(event);
        } else {
            savePatientProfile(event);
        }
    }

    private void savePatientProfile(ActionEvent event) {
        String phone = phoneField.getText();
        String bloodGroup = bloodGroupCombo.getValue();
        String address = addressField.getText();
        String allergies = allergiesCheck.isSelected() ? "Yes" : "No";
        String diabetes = diabetesCheck.isSelected() ? "Yes" : "No";
        String history = historyArea.getText();

        if (phone.isEmpty() || bloodGroup == null || address.isEmpty()) {
            showAlert("Validation Error", "Please fill all mandatory fields (Phone, Blood Group, Address).");
            return;
        }

        String sql = "INSERT INTO patients (user_id, phone, blood_group, address, allergies, diabetes, medical_history, profile_completed) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, 1) " +
                     "ON CONFLICT(user_id) DO UPDATE SET " +
                     "phone=excluded.phone, blood_group=excluded.blood_group, address=excluded.address, " +
                     "allergies=excluded.allergies, diabetes=excluded.diabetes, medical_history=excluded.medical_history, " +
                     "profile_completed=1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, SessionManager.getUserId());
            pstmt.setString(2, phone);
            pstmt.setString(3, bloodGroup);
            pstmt.setString(4, address);
            pstmt.setString(5, allergies);
            pstmt.setString(6, diabetes);
            pstmt.setString(7, history);

            pstmt.executeUpdate();
            loadScene(event, "/fxml/patient-dashboard.fxml");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void saveDoctorProfile(ActionEvent event) {
        String spec = specializationField.getText();
        String qual = qualificationField.getText();
        String exp = experienceField.getText();
        String phone = docPhoneField.getText();
        String feeStr = feeField.getText();

        if (spec.isEmpty() || qual.isEmpty() || exp.isEmpty() || phone.isEmpty() || feeStr.isEmpty()) {
            showAlert("Validation Error", "Please fill all fields.");
            return;
        }

        double fee;
        try {
            fee = Double.parseDouble(feeStr);
        } catch (NumberFormatException e) {
            showAlert("Input Error", "Fee must be a number.");
            return;
        }

        String sql = "INSERT INTO doctors (user_id, specialization, qualification, experience, phone, fee, profile_completed) " +
                     "VALUES (?, ?, ?, ?, ?, ?, 1) " +
                     "ON CONFLICT(user_id) DO UPDATE SET " +
                     "specialization=excluded.specialization, qualification=excluded.qualification, " +
                     "experience=excluded.experience, phone=excluded.phone, fee=excluded.fee, " +
                     "profile_completed=1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, SessionManager.getUserId());
            pstmt.setString(2, spec);
            pstmt.setString(3, qual);
            pstmt.setString(4, exp);
            pstmt.setString(5, phone);
            pstmt.setDouble(6, fee);

            pstmt.executeUpdate();
            System.out.println("[DEBUG_LOG] Doctor profile saved successfully for user ID: " + SessionManager.getUserId());
            loadScene(event, "/fxml/doctor-dashboard.fxml");

        } catch (SQLException e) {
            System.err.println("[DEBUG_LOG] Failed to save doctor profile: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void loadScene(ActionEvent event, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(loader.load(),
                    stage.getWidth() > 0 ? stage.getWidth() : SceneManager.AUTH_WIDTH,
                    stage.getHeight() > 0 ? stage.getHeight() : SceneManager.AUTH_HEIGHT);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
