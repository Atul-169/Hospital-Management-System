package com.example.project.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class SceneManager {

    /**
     * This method switches the scene based on the source node and the FXML path.
     * @param sourceNode The node that triggered the scene switch (to get the window).
     * @param fxmlPath The path to the new FXML file.
     */
    public static void switchScene(Node sourceNode, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlPath));
            Scene scene = new Scene(loader.load(), 1024, 768);
            Stage stage = (Stage) sourceNode.getScene().getWindow();
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            System.err.println("[DEBUG_LOG] Error switching scene to " + fxmlPath + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
