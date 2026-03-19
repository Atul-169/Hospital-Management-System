package com.example.project;

import com.example.project.util.DBInitializer;
import com.example.project.util.SceneManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // অ্যাপ স্টার্ট হওয়ার সাথে সাথে ডাটাবেজ ইনিশিয়ালাইজ করা
        DBInitializer.initialize();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/splash.fxml"));
        Scene scene = new Scene(loader.load(), SceneManager.AUTH_WIDTH, SceneManager.AUTH_HEIGHT);

        stage.setTitle("BUET MedTech");
        stage.setScene(scene);
        stage.show();
    }
}
