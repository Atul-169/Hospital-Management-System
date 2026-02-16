package com.example.project;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {

        FXMLLoader loader = new FXMLLoader(
                HelloApplication.class.getResource("/fxml/login.fxml")
        );
        Scene scene = new Scene(loader.load(), 800, 500);

// Only add CSS if it's found
URL cssURL = HelloApplication.class.getResource("/css/style.css");
if (cssURL != null) {
    scene.getStylesheets().add(cssURL.toExternalForm());
} else {
    System.out.println("CSS not found!");
}
        stage.setTitle("CureCoders");
        stage.setScene(scene);
        stage.show();

    }
    private void showAlert(String title, String message) {
        javafx.scene.control.Alert alert =
                new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
