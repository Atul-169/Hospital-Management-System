package com.example.project.controller;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import com.example.project.util.SceneManager;

public class RoleSelectionController {

    @FXML private VBox rightCard;
    @FXML private Button adminBtn;
    @FXML private Button doctorBtn;
    @FXML private Button patientBtn;

    @FXML
    public void initialize() {
        if (rightCard != null) {
            rightCard.setOpacity(0);
            rightCard.setTranslateX(36);
            rightCard.setScaleX(0.97);
            rightCard.setScaleY(0.97);

            FadeTransition fade = new FadeTransition(Duration.millis(850), rightCard);
            fade.setFromValue(0);
            fade.setToValue(1);
            fade.setInterpolator(Interpolator.EASE_BOTH);

            TranslateTransition slide = new TranslateTransition(Duration.millis(850), rightCard);
            slide.setFromX(36);
            slide.setToX(0);
            slide.setInterpolator(Interpolator.EASE_BOTH);

            ScaleTransition scale = new ScaleTransition(Duration.millis(850), rightCard);
            scale.setFromX(0.97);
            scale.setFromY(0.97);
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.setInterpolator(Interpolator.EASE_BOTH);

            new ParallelTransition(fade, slide, scale).play();
        }

        installHoverAnimation(adminBtn);
        installHoverAnimation(doctorBtn);
        installHoverAnimation(patientBtn);
        Platform.runLater(() -> {
            rightCard.getScene().setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ESCAPE) {
                    // go back to splash or close
                    Stage stage = (Stage) rightCard.getScene().getWindow();
                    stage.close();
                }
            });
        });
    }

    private void installHoverAnimation(Button btn) {
        if (btn == null) return;

        DropShadow glow = new DropShadow();
        glow.setColor(Color.rgb(59, 130, 246, 0.55));
        glow.setRadius(18);

        btn.setOnMouseEntered(e -> {
            ScaleTransition s = new ScaleTransition(Duration.millis(180), btn);
            s.setToX(1.05);
            s.setToY(1.05);
            s.setInterpolator(Interpolator.EASE_BOTH);
            s.play();
            btn.setEffect(glow);
        });

        btn.setOnMouseExited(e -> {
            ScaleTransition s = new ScaleTransition(Duration.millis(180), btn);
            s.setToX(1.0);
            s.setToY(1.0);
            s.setInterpolator(Interpolator.EASE_BOTH);
            s.play();
            btn.setEffect(null);
        });
    }

    public void handleAdminSelection(ActionEvent event) {
        com.example.project.util.SessionManager.setSelectedRole("Admin");
        openLogin(event);
    }

    public void handleDoctorSelection(ActionEvent event) {
        com.example.project.util.SessionManager.setSelectedRole("Doctor");
        openLogin(event);
    }

    public void handlePatientSelection(ActionEvent event) {
        com.example.project.util.SessionManager.setSelectedRole("Patient");
        openLogin(event);
    }

    private void openLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            String fxmlPath = "/fxml/login.fxml";
            Scene scene = SceneManager.createScene(loader.load(), fxmlPath);
            stage.setScene(scene);
            SceneManager.configureStage(stage, fxmlPath);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
