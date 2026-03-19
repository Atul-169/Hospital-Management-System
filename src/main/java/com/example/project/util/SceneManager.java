package com.example.project.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class SceneManager {
    public static final double AUTH_WIDTH = 1024;
    public static final double AUTH_HEIGHT = 768;
    public static final double APP_WIDTH = 1280;
    public static final double APP_HEIGHT = 820;

    /**
     * This method switches the scene based on the source node and the FXML path.
     * @param sourceNode The node that triggered the scene switch (to get the window).
     * @param fxmlPath The path to the new FXML file.
     */
    public static void switchScene(Node sourceNode, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlPath));
            Stage stage = (Stage) sourceNode.getScene().getWindow();
            double width = stage.getWidth() > 0 ? stage.getWidth() : APP_WIDTH;
            double height = stage.getHeight() > 0 ? stage.getHeight() : APP_HEIGHT;
            Scene scene = new Scene(loader.load(), width, height);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            System.err.println("[DEBUG_LOG] Error switching scene to " + fxmlPath + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
