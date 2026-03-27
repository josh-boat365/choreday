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
        signUpButton.setOnAction(event -> {
            String fullName = fullNameField.getText();
            String studentId = studentIdField.getText();
            String password = passwordField.getText();
            String confirmPassword = confirmPasswordField.getText();

            if (password.equals(confirmPassword)) {
                loadingIndicator.setVisible(true);
                signUpButton.setDisable(true);

                new Thread(() -> {
                    UserModel user = signUpService.signUp(fullName, studentId, password);

                    Platform.runLater(() -> {
                        loadingIndicator.setVisible(false);
                        signUpButton.setDisable(false);

                        if (user != null) {
                            System.out.println("Sign Up Successful!");
                            SessionManager.setCurrentUser(user);
                            Navigator.navigateTo("SignIn.fxml", fullNameField, null, null);
                        } else {
                            showAlert(Alert.AlertType.ERROR, "Registration Error", "Could not save user to the database.");
                        }
                    });
                }).start();
            } else {
                showAlert(Alert.AlertType.WARNING, "Password Mismatch", "Passwords do not match. Please try again.");
            }
        });

        signInText.setOnAction(event -> {
            Navigator.navigateTo("SignIn.fxml", fullNameField, null, null);
        });

        if (backToLoginButton != null) {
            backToLoginButton.setOnAction(event -> {
                Navigator.navigateTo("SignIn.fxml", fullNameField, null, null);
            });
        }
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
