package com.example.project;   // ⚠ must match your real package

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.stage.Stage;

import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;

import javafx.event.ActionEvent;
import javafx.scene.control.Hyperlink;


import java.io.IOException;

public class RoleLoginController {

    @FXML
    private Label titleLabel;

    @FXML

    private Hyperlink registerLink;

    private String role;

    public void setRole(String role) {
        this.role = role;
        titleLabel.setText(role + " Login");
        if (role.equals("Guest")) {
            registerLink.setVisible(false);
            registerLink.setManaged(false);
        }
    }

    @FXML
    private void handleLogin() {

//        if(role.equals("Doctor")) {
//            openDoctorDashboard();
//        }
//        else if(role.equals("Patient")) {
//            openPatientDashboard();
//        }
//        else {
//            openGuestDashboard();
//        }
        System.out.println("Login Done");
    }
@FXML
private void goBack(ActionEvent event) throws IOException {
    loadScene(event, "/fxml/login.fxml");
}

    // 🔥 Reusable Scene Loader
    private void loadScene(ActionEvent event, String path) throws IOException {

        FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
        Scene scene = new Scene(loader.load());

        Stage stage = (Stage) ((Node) event.getSource())
                .getScene().getWindow();

        stage.setScene(scene);
        stage.show();
    }

    @FXML
    private void openRegister(ActionEvent event) throws IOException {

        if (role.equals("Doctor")) {
            loadScene(event, "/fxml/doctor-register.fxml");
        }
        else if (role.equals("Patient")) {
            loadScene(event, "/fxml/patient-register.fxml");
        }
    }
}

