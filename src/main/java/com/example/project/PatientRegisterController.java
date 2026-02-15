package com.example.project;



import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.stage.Stage;

import java.io.IOException;

public class PatientRegisterController {

    @FXML private TextField fullName;
    @FXML private TextField email;
    @FXML private TextField phone;
    @FXML private TextField bloodGroup;
    @FXML private TextField age;
    @FXML private TextField gender;
    @FXML private TextField address;
    @FXML private TextField emergencyContact;
    @FXML private TextField previousDisease;
    @FXML private TextField allergies;
    @FXML private PasswordField password;

    @FXML
    private void handleRegister() {
        System.out.println("Patient Registered: " + fullName.getText());
        // Later save to database
    }

    @FXML
    private void goBack(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/role-login.fxml")
        );
        Scene scene = new Scene(loader.load());
        Stage stage = (Stage)((Node)event.getSource())
                .getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }
}

