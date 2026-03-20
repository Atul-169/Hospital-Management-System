package com.example.project.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneManager {
    public static final double SPLASH_WIDTH = 800;
    public static final double SPLASH_HEIGHT = 500;
    public static final double AUTH_WIDTH = 1024;
    public static final double AUTH_HEIGHT = 768;
    public static final double APP_WIDTH = 1280;
    public static final double APP_HEIGHT = 820;

    public static void switchScene(Node sourceNode, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) sourceNode.getScene().getWindow();
            stage.setScene(createScene(root, fxmlPath));
            configureStage(stage, fxmlPath);
            stage.show();
        } catch (IOException e) {
            System.err.println("[DEBUG_LOG] Error switching scene to " + fxmlPath + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static Scene createScene(Parent root, String fxmlPath) {
        double[] size = getSceneSize(fxmlPath);
        return new Scene(root, size[0], size[1]);
    }

    public static void configureStage(Stage stage, String fxmlPath) {
        double[] size = getSceneSize(fxmlPath);
        stage.setWidth(size[0]);
        stage.setHeight(size[1]);
        stage.setMinWidth(size[0]);
        stage.setMaxWidth(size[0]);
        stage.setMinHeight(size[1]);
        stage.setMaxHeight(size[1]);
        stage.setResizable(false);
        stage.centerOnScreen();
    }

    private static double[] getSceneSize(String fxmlPath) {
        if ("/fxml/splash.fxml".equals(fxmlPath)) {
            return new double[]{SPLASH_WIDTH, SPLASH_HEIGHT};
        }
        if (fxmlPath != null && fxmlPath.contains("dashboard")) {
            return new double[]{APP_WIDTH, APP_HEIGHT};
        }
        return new double[]{AUTH_WIDTH, AUTH_HEIGHT};
    }
}
