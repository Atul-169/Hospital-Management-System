package com.example.project.controller;

import com.example.project.util.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class PatientDashboardController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label userNameLabel;

    @FXML
    private Label bloodGroupLabel; // Optional: To show blood group in stat card

    @FXML
    public void initialize() {
        // সেশন থেকে ইউজারনেম নেওয়া
        String userName = SessionManager.getUserName();
        if (userName != null && !userName.isEmpty()) {
            userNameLabel.setText(userName);
            welcomeLabel.setText("Welcome back, " + userName + "!");
        } else {
            userNameLabel.setText("Patient User");
            welcomeLabel.setText("Welcome back, Patient!");
        }
        // Could load more stats from DB here
    }

    @FXML
    public void handleProfileUpdate(javafx.scene.input.MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/profile-setup.fxml"));
            Scene scene = new Scene(loader.load(), 1024, 768);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        try {
            SessionManager.clearSession();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/role-selection.fxml"));
            Scene scene = new Scene(loader.load(), 1024, 768);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
