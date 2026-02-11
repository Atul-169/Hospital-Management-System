package com.example.project;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class LoginController {

    @FXML
    private VBox rightPane; // The right-side VBox

    @FXML
    private void initialize() {
        // Optional: you can set initial state here
    }

    @FXML
    private void doctorLogin() {
        showLoginForm("Doctor Login");
    }

    @FXML
    private void patientLogin() {
        showLoginForm("Patient Login");
    }

    @FXML
    private void guestLogin() {
        showLoginForm("Guest Access");
    }

    // ------------------- Show Login Form -------------------
    private void showLoginForm(String titleText) {
        rightPane.getChildren().clear();

        // Title
        Label title = new Label(titleText);
        title.getStyleClass().add("login-title");

        // Username field
        Label usernameLabel = new Label("Username:");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter username");

        VBox form = new VBox(10, usernameLabel, usernameField);

        // Password field for Doctor/Patient (not Guest)
        if (!titleText.equals("Guest Access")) {
            Label passwordLabel = new Label("Password:");
            PasswordField passwordField = new PasswordField();
            passwordField.setPromptText("Enter password");
            form.getChildren().addAll(passwordLabel, passwordField);
        }

        // Login Button
        Button loginBtn = new Button("Login");
        loginBtn.getStyleClass().add("login-btn");
        loginBtn.setOnAction(e -> {
            System.out.println(titleText + " clicked: " + usernameField.getText());
            // TODO: actual login logic
        });

        // Create account link
        Hyperlink createAccount = new Hyperlink("Don't have an account? Create one");
        createAccount.setOnAction(e -> showCreateAccountForm(titleText));

        // Back Button
        Button backBtn = new Button("Back");
        backBtn.getStyleClass().add("back-btn");
        backBtn.setOnAction(e -> showMainLoginOptions());

        rightPane.getChildren().addAll(title, form, loginBtn, createAccount, backBtn);
    }

    // ------------------- Show Create Account Form -------------------
    private void showCreateAccountForm(String forUser) {
        rightPane.getChildren().clear();

        Label title = new Label("Create Account (" + forUser + ")");
        title.getStyleClass().add("login-title");

        // Form fields
        Label usernameLabel = new Label("Username:");
        TextField usernameField = new TextField();

        Label passwordLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();

        Label confirmLabel = new Label("Confirm Password:");
        PasswordField confirmField = new PasswordField();

        VBox form = new VBox(10, usernameLabel, usernameField,
                passwordLabel, passwordField,
                confirmLabel, confirmField);

        // Create Account Button
        Button createBtn = new Button("Create Account");
        createBtn.getStyleClass().add("login-btn");
        createBtn.setOnAction(e -> {
            System.out.println("Account created for: " + usernameField.getText());
            // TODO: Add logic to save the account
            showLoginForm(forUser); // return to login after creation
        });

        // Back button
        Button backBtn = new Button("Back");
        backBtn.getStyleClass().add("back-btn");
        backBtn.setOnAction(e -> showLoginForm(forUser));

        rightPane.getChildren().addAll(title, form, createBtn, backBtn);
    }

    // ------------------- Show Main Login Options -------------------
    private void showMainLoginOptions() {
        rightPane.getChildren().clear();

        Label title = new Label("Login As");
        title.getStyleClass().add("login-title");

        Button doctorBtn = new Button("Doctor");
        doctorBtn.getStyleClass().add("login-btn");
        doctorBtn.setOnAction(e -> doctorLogin());

        Button patientBtn = new Button("Patient");
        patientBtn.getStyleClass().add("login-btn");
        patientBtn.setOnAction(e -> patientLogin());

        Button guestBtn = new Button("Guest");
        guestBtn.getStyleClass().add("login-btn");
        guestBtn.setOnAction(e -> guestLogin());

        rightPane.getChildren().addAll(title, doctorBtn, patientBtn, guestBtn);
    }
}
