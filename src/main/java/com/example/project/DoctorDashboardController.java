package com.example.project;

import com.example.project.firebase.FirebaseService;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class DoctorDashboardController {

    @FXML public BorderPane mainPane;
    @FXML private StackPane contentArea;
    @FXML private VBox dashboardView;
    @FXML private Label welcomeLabel, doctorName, topDoctorName, totalPatients, pendingReports;
    @FXML private VBox surgeryBox;

    // Report Update Fields
    @FXML private TextField patientIdSearch;
    @FXML private TextArea reportArea;

    public void setUsername(String name) {
        String title = "Dr. " + name;
        welcomeLabel.setText("Welcome, " + title);
        doctorName.setText(title);
        topDoctorName.setText(title);

        // Show surgery tab only if the doctor is a surgeon
        if (name.toLowerCase().contains("surgeon")) {
            surgeryBox.setVisible(true);
            surgeryBox.setManaged(true);
        }
    }

    @FXML
    private void handleUpdateReport() {
        String pId = patientIdSearch.getText().trim();
        String report = reportArea.getText().trim();

        if (pId.isEmpty() || report.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Input Required", "Please provide both Patient ID and the Report content.");
            return;
        }

        try {
            // Push to Firebase via Service
            FirebaseService.updateReport(pId, report);

            showAlert(Alert.AlertType.INFORMATION, "Success", "Patient report has been updated successfully.");
            patientIdSearch.clear();
            reportArea.clear();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update report: " + e.getMessage());
        }
    }

    @FXML
    private void showDashboard() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(dashboardView);
    }

    @FXML
    private void showAppointments() {
        updateCenterView("My Appointments", new String[]{
                "10:00 AM - John Doe (Checkup)",
                "11:30 AM - Jane Smith (Follow-up)",
                "02:00 PM - Robert Brown (Consultation)"
        });
    }

    @FXML
    private void showPatients() {
        updateCenterView("Assigned Patients", new String[]{
                "John Doe (ID: 1001)",
                "Jane Smith (ID: 1002)",
                "Robert Brown (ID: 1003)",
                "Alice Wilson (ID: 1004)"
        });
    }

    @FXML
    private void showSurgeries() {
        updateCenterView("Scheduled Surgeries", new String[]{
                "OR-1: Appendectomy (Dr. " + doctorName.getText() + ")",
                "OR-3: Knee Replacement (Tomorrow)"
        });
    }

    private void updateCenterView(String title, String[] data) {
        VBox container = new VBox(20);
        container.setStyle("-fx-padding: 30;");

        Label lblTitle = new Label(title);
        lblTitle.setStyle("-fx-font-size: 22; -fx-font-weight: bold;");

        ListView<String> listView = new ListView<>(FXCollections.observableArrayList(data));
        listView.setPrefHeight(400);

        container.getChildren().addAll(lblTitle, listView);
        contentArea.getChildren().clear();
        contentArea.getChildren().add(container);
    }

    @FXML
    private void handleLogout(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/login.fxml")));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root, 800, 500);

        // Ensure CSS persists
        if (getClass().getResource("/css/style.css") != null) {
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        }

        stage.setScene(scene);
        stage.show();
    }

    @FXML
    private void handleEditProfile() {
        showAlert(Alert.AlertType.INFORMATION, "Profile Settings", "Edit Profile module will be available in the next update.");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}