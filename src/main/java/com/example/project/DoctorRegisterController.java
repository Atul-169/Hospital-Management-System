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

public class DoctorRegisterController {

    @FXML private TextField fullName;
    @FXML private TextField email;
    @FXML private TextField phone;
    @FXML private TextField licenseNumber;
    @FXML private TextField specialization;
    @FXML private TextField experienceYears;
    @FXML private TextField hospitalReference;
    @FXML private TextField address;
    @FXML private TextField qualification;
    @FXML private PasswordField password;

    @FXML
    private void handleRegister() {
        System.out.println("Doctor Registered: " + fullName.getText());
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

