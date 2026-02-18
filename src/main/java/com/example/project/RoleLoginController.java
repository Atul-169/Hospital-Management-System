package com.example.project;

import com.example.project.firebase.FirebaseService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
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


    public void setRole(String role) {
        this.role = role;
        titleLabel.setText(role + " Login");

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

    //LOGIN logic
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
            String fxmlPath = "/fxml/" + userRole.toLowerCase() + "-dashboard.fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            if (userRole.equalsIgnoreCase("Doctor")) {
                DoctorDashboardController controller = loader.getController();
                controller.setUsername(input);
            } else if (userRole.equalsIgnoreCase("Patient")) {
                PatientDashboardController controller = loader.getController();
                controller.setUsername(input);
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root,800,500));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Something went wrong.");
        }
    }



    //Open Register
    @FXML
    private void openRegister(ActionEvent event) throws IOException {

        if (role.equals("Doctor")) {
            loadScene(event, "/fxml/doctor-register.fxml");
        }
        else if (role.equals("Patient")) {
            loadScene(event, "/fxml/patient-register.fxml");
        }
    }


    @FXML
    private void goBack(ActionEvent event) throws IOException {
        loadScene(event, "/fxml/login.fxml");
    }


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
