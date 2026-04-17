package org.group45.choreday.controllers;

import org.group45.choreday.utils.Navigator;
import org.group45.choreday.utils.SessionManager;
import org.group45.choreday.models.UserModel;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;

import org.group45.choreday.services.SignUpService;

public class SignUpController {

    private SignUpService signUpService = new SignUpService();

    @FXML
    private TextField fullNameField;

    @FXML
    private TextField studentIdField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Button signUpButton;

    @FXML
    private Button backToLoginButton;

    @FXML
    private Button signInText;

    @FXML
    private ProgressIndicator loadingIndicator;

    @FXML
    public void initialize() {
        signUpButton.setOnAction(event -> handleSignUp());

        signInText.setOnAction(event -> {
            Navigator.navigateTo("SignIn.fxml", fullNameField, null, null);
        });

        if (backToLoginButton != null) {
            backToLoginButton.setOnAction(event -> {
                Navigator.navigateTo("SignIn.fxml", fullNameField, null, null);
            });
        }
    }

    private void handleSignUp() {
        String fullName = fullNameField.getText().trim();
        String studentId = studentIdField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validation
        if (fullName.isEmpty() || studentId.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "⚠ Missing Information",
                "Please fill in all the fields:\n• Full Name\n• Student ID\n• Password\n• Password Confirmation");
            return;
        }

        if (fullName.length() < 3) {
            showAlert(Alert.AlertType.WARNING, "⚠ Invalid Name",
                "Full Name must be at least 3 characters long.");
            return;
        }

        if (studentId.length() < 3) {
            showAlert(Alert.AlertType.WARNING, "⚠ Invalid Student ID",
                "Student ID must be at least 3 characters long.");
            return;
        }

        if (password.length() < 4) {
            showAlert(Alert.AlertType.WARNING, "⚠ Weak Password",
                "Password must be at least 4 characters long.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showAlert(Alert.AlertType.WARNING, "⚠ Password Mismatch",
                "The passwords you entered do not match.\n\nPlease try again.");
            confirmPasswordField.clear();
            passwordField.clear();
            passwordField.requestFocus();
            return;
        }

        // Proceed with registration
        loadingIndicator.setVisible(true);
        signUpButton.setDisable(true);

        new Thread(() -> {
            UserModel user = signUpService.signUp(fullName, studentId, password);

            Platform.runLater(() -> {
                loadingIndicator.setVisible(false);
                signUpButton.setDisable(false);

                if (user != null) {
                    showAlert(Alert.AlertType.INFORMATION, "✓ Account Created!",
                        "Your account has been created successfully.\n\nWelcome, " + fullName + "!");
                    SessionManager.setCurrentUser(user);
                    Navigator.navigateTo("SignIn.fxml", fullNameField, null, null);
                } else {
                    showAlert(Alert.AlertType.ERROR, "✗ Registration Failed",
                        "Could not create your account.\n\nThe Student ID may already be in use.\nPlease try a different one.");
                    studentIdField.clear();
                    studentIdField.requestFocus();
                }
            });
        }).start();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.getDialogPane().getStylesheets().add(getClass().getResource("/org/group45/choreday/style.css").toExternalForm());
        alert.getDialogPane().getStyleClass().add("dialog-pane");
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
