package com.example.project;

import com.example.project.firebase.FirebaseService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Hyperlink;
import javafx.stage.Stage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


import java.io.IOException;

public class RoleLoginController {

    @FXML
    private Label titleLabel;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Hyperlink registerLink;

    private String role;


    // 🔹 Called from previous screen
    public void setRole(String role) {
        this.role = role;
        titleLabel.setText(role + " Login");

        // Hide register for Guest
        if (role.equals("Guest")) {
            registerLink.setVisible(false);
            registerLink.setManaged(false);
        }
    }
    private void showAlert(String title, String message) {
        javafx.scene.control.Alert alert =
                new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // 🔐 LOGIN LOGIC
    @FXML
    private void handleLogin(ActionEvent event) {

        String input = usernameField.getText();
        String passwordInput = passwordField.getText();

        if (input.isEmpty() || passwordInput.isEmpty()) {
            showAlert("Error", "Please fill all fields.");
            return;
        }

        try {

            String emailToUse;

            if (input.contains("@")) {
                emailToUse = input;
            } else {
                emailToUse = FirebaseService.getEmailByUsername(input);

                if (emailToUse == null) {
                    showAlert("Login Error", "Username not found.");
                    return;
                }
            }

            String response = FirebaseService.loginUser(
                    emailToUse,
                    passwordInput
            );

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(response);

            if (node.has("error")) {
                String message = node.get("error").get("message").asText();
                showAlert("Login Error", message);
                return;
            }

            String uid = node.get("localId").asText();

            String userRole = FirebaseService.getUserRole(uid);

            if (userRole == null) {
                showAlert("Error", "Role not found.");
                return;
            }
            showAlert("Success Login","SUcess success");            loadScene(event, "/fxml/" + userRole.toLowerCase() + "-dashboard.fxml");

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Something went wrong.");
        }
    }



    // 🔹 Open Register Page
    @FXML
    private void openRegister(ActionEvent event) throws IOException {

        if (role.equals("Doctor")) {
            loadScene(event, "/fxml/doctor-register.fxml");
        }
        else if (role.equals("Patient")) {
            loadScene(event, "/fxml/patient-register.fxml");
        }
    }


    // 🔹 Back to Role Selection
    @FXML
    private void goBack(ActionEvent event) throws IOException {
        loadScene(event, "/fxml/login.fxml");
    }


    // 🔹 Scene Loader Helper
    private void loadScene(ActionEvent event, String path) throws IOException {

        FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
        Scene scene = new Scene(loader.load());

        Stage stage = (Stage) ((Node) event.getSource())
                .getScene()
                .getWindow();

        stage.setScene(scene);
        stage.show();
    }
}
