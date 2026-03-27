package org.group45.choreday.screen;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import org.group45.choreday.utils.Navigator;

public class LoginScreen {

    @FXML
    private TextField studentIdField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button signInButton;

    @FXML
    private Button signUpText;

    @FXML
    public void initialize() {
        signInButton.setOnAction(event -> {
            Navigator.navigateTo("Dashboard.fxml", passwordField, null, null);
        });

        // Set action for clicking the sign-up text/button
        signUpText.setOnAction(event -> {
            Navigator.navigateTo("SignUp.fxml", passwordField, null, null);
        });
    }
}
