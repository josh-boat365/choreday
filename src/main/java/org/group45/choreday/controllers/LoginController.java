package org.group45.choreday.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import org.group45.choreday.models.UserModel;
import org.group45.choreday.services.SignInService;
import org.group45.choreday.utils.SessionManager;
import org.group45.choreday.utils.Navigator;

public class LoginController {

    private SignInService signInService = new SignInService();

    @FXML
    private TextField studentIdField;

    @FXML private PasswordField passwordField;
    @FXML private Button signInButton;
    @FXML private Button signUpText;
    @FXML private ProgressIndicator loadingIndicator;

    @FXML
    public void initialize() {
        signInButton.setOnAction(event -> handleSignIn());

        signUpText.setOnAction(event -> {
            Navigator.navigateTo("SignUp.fxml", studentIdField, null, null);
        });
    }

    private void handleSignIn() {
        String studentId = studentIdField.getText().trim();
        String password = passwordField.getText();

        // Validation
        if (studentId.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "⚠ Missing Information", 
                "Please enter both your Student ID and Password.");
            return;
        }

        if (studentId.length() < 3) {
            showAlert(Alert.AlertType.WARNING, "⚠ Invalid Input", 
                "Student ID must be at least 3 characters long.");
            return;
        }

        // Show loading and disable buttons
        loadingIndicator.setVisible(true);
        signInButton.setDisable(true);

        new Thread(() -> {
            UserModel user = signInService.signIn(studentId, password);
            
            Platform.runLater(() -> {
                loadingIndicator.setVisible(false);
                signInButton.setDisable(false);

                if (user != null) {
                    showAlert(Alert.AlertType.INFORMATION, "✓ Welcome!", 
                        "Sign in successful. Loading your dashboard...");
                    SessionManager.setCurrentUser(user);
                    Navigator.navigateTo("Dashboard.fxml", studentIdField, null, null);
                } else {
                    showAlert(Alert.AlertType.ERROR, "✗ Login Failed", 
                        "Invalid Student ID or Password.\n\nPlease check your credentials and try again.");
                    passwordField.clear();
                    passwordField.requestFocus();
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
