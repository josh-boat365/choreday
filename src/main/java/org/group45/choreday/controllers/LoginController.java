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
        signInButton.setOnAction(event -> {
            String studentId = studentIdField.getText();
            String password = passwordField.getText();

            // Show loading and disable buttons
            loadingIndicator.setVisible(true);
            signInButton.setDisable(true);

            new Thread(() -> {
                UserModel user = signInService.signIn(studentId, password);
                
                Platform.runLater(() -> {
                    loadingIndicator.setVisible(false);
                    signInButton.setDisable(false);

                    if (user != null) {
                        SessionManager.setCurrentUser(user);
                        Navigator.navigateTo("Dashboard.fxml", studentIdField, null, null);
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid student ID or password.");
                    }
                });
            }).start();
        });

        signUpText.setOnAction(event -> {
            Navigator.navigateTo("SignUp.fxml", studentIdField, null, null);
        });
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
