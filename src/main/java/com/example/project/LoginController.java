package com.example.project;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.stage.Stage;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class LoginController {


    @FXML
    private void doctorLogin(ActionEvent event) throws IOException {
        openLogin(event, "Doctor");
    }

    @FXML
    private void patientLogin(ActionEvent event) throws IOException {
        openLogin(event, "Patient");
    }

    @FXML
    private void guestLogin(ActionEvent event) throws IOException {
        openLogin(event, "Guest");
    }



    private void openLogin(ActionEvent event, String role) throws IOException {

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/role-login.fxml")
        );

        Scene scene = new Scene(loader.load());

        RoleLoginController controller = loader.getController();
        controller.setRole(role);

        Stage stage = (Stage) ((Node) event.getSource())
                .getScene().getWindow();

        stage.setScene(scene);
        stage.show();
    }
}
