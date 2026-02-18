package com.example.project;

import com.example.project.firebase.FirebaseService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.management.relation.Role;
import java.io.IOException;

public class PatientRegisterController {

    @FXML
    private TextField email;

    @FXML
    private TextField username;

    @FXML
    private PasswordField password;

    @FXML
    private PasswordField confirmPassword;

    private void showAlert(String title, String message) {
        javafx.scene.control.Alert alert =
                new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    @FXML
    private void handleRegister(ActionEvent event) {

        if (email.getText().isEmpty() ||
                username.getText().isEmpty() ||
                password.getText().isEmpty() ||
                confirmPassword.getText().isEmpty()) {

                showAlert("Registration Error","Fields can't be empty");

                return;
        }

        if (!password.getText().equals(confirmPassword.getText())) {
            showAlert("Registration Error","Password doesn't match");
            return;
        }

        try {

            //Registerin Firebase
            String response = FirebaseService.registerUser(
                    email.getText(),
                    password.getText()
            );

            if (!response.contains("localId")) {
                System.out.println("Registration failed: " + response);
                return;
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(response);

            if (node.has("error")) {
                String message = node.get("error").get("message").asText();
                showAlert("Registration Error", message);
                return;
            }

            String uid = node.get("localId").asText();


            // Save profile in Database
            FirebaseService.saveUserProfile(
                    uid,
                    email.getText(),
                    username.getText(),
                    "Patient"
            );

            showAlert("Registration Successfull","Congrats you are a coder patient now!");

            goBack(event);

        } catch (Exception e) {
            showAlert("Error",e.getMessage());
            e.printStackTrace();
        }
    }


    @FXML
    private void goBack(ActionEvent event) throws IOException {

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/role-login.fxml")
        );

        Scene scene = new Scene(loader.load());

        Stage stage = (Stage) ((Node) event.getSource())
                .getScene().getWindow();

        stage.setScene(scene);
        stage.show();
    }
}