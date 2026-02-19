package com.example.project;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;

public class PatientDashboardController {

    @FXML private StackPane contentArea;
    @FXML private VBox homeView;
    @FXML private Label welcomeLabel, topPatientName, sidePatientName, upcomingCount, reportsCount;

    /**
     * Called by RoleLoginController to pass the logged-in user's name
     */
    public void setUsername(String name) {
        welcomeLabel.setText("Welcome back, " + name + "!");
        topPatientName.setText(name);
        sidePatientName.setText(name);
    }

    // --- Navigation Logic ---

    @FXML
    private void showHome() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(homeView);
    }

    @FXML
    private void showFindDoctor() {
        updateCenterView("Search for Specialist", new String[]{
                "Dr. Smith - Cardiologist",
                "Dr. Sarah - Neurologist",
                "Dr. Ahmed - General Surgeon",
                "Dr. Miller - Pediatrist"
        });
    }

    @FXML
    private void showRecords() {
        updateCenterView("My Medical History", new String[]{
                "Blood Test Results (Jan 2024)",
                "X-Ray Report (Dec 2023)",
                "Prescription - Dr. Smith"
        });
    }

    @FXML
    private void showMyAppointments() {
        updateCenterView("My Scheduled Visits", new String[]{
                "Tomorrow 10:00 AM with Dr. Smith",
                "Next Friday 03:00 PM with Dr. Sarah"
        });
    }

    /**
     * Reusable helper to swap the center content with a Title and a ListView
     */
    private void updateCenterView(String title, String[] items) {
        VBox container = new VBox(20);
        container.setStyle("-fx-padding: 30;");

        Label lblTitle = new Label(title);
        lblTitle.setStyle("-fx-font-size: 22; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        ListView<String> listView = new ListView<>(FXCollections.observableArrayList(items));
        listView.setPrefHeight(400);

        container.getChildren().addAll(lblTitle, listView);

        contentArea.getChildren().clear();
        contentArea.getChildren().add(container);
    }

    @FXML
    private void handleLogout(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        Scene scene = new Scene(root, 800, 500);
        // Ensure CSS is reapplied
        String css = getClass().getResource("/css/style.css").toExternalForm();
        scene.getStylesheets().add(css);

        stage.setScene(scene);
        stage.show();
    }

    @FXML
    private void handleEditProfile() {
        System.out.println("Opening Profile Settings...");
    }

}