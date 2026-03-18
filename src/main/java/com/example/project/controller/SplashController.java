package com.example.project.controller;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

public class SplashController {

    @FXML
    private ImageView logoImage;

    @FXML
    public void initialize() {
        Image logo = new Image(getClass().getResourceAsStream("/images/logo.png"));
        logoImage.setImage(logo);

        DropShadow glow = new DropShadow();
        glow.setColor(Color.CYAN);
        glow.setRadius(40);
        glow.setSpread(0.35);
        logoImage.setEffect(glow);

        FadeTransition fade = new FadeTransition(Duration.seconds(1.5), logoImage);
        fade.setFromValue(0.55);
        fade.setToValue(1.0);
        fade.setCycleCount(FadeTransition.INDEFINITE);
        fade.setAutoReverse(true);
        fade.play();

        ScaleTransition scale = new ScaleTransition(Duration.seconds(1.5), logoImage);
        scale.setFromX(0.92);
        scale.setFromY(0.92);
        scale.setToX(1.05);
        scale.setToY(1.05);
        scale.setCycleCount(ScaleTransition.INDEFINITE);
        scale.setAutoReverse(true);
        scale.play();

        PauseTransition delay = new PauseTransition(Duration.seconds(5));
        delay.setOnFinished(event -> openRoleSelection());
        delay.play();
    }

    private void openRoleSelection() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/role-selection.fxml"));
            Scene scene = new Scene(loader.load(), 1024, 768);
            Stage stage = (Stage) logoImage.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}