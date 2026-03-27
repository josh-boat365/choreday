package org.group45.choreday.screen;

import org.group45.choreday.utils.Navigator;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class SignUpScreen {

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
    private Button signInText;

    @FXML
    public void initialize() {
        // Example: How to access or update values
        // fullNameField.setText("Default Name");
        // String password = passwordField.getText();

        signUpButton.setOnAction(event -> {
            System.out.println("Sign Up Button Clicked");
        });

        signInText.setOnAction(event -> {
            Navigator.navigateTo("SignIn.fxml", passwordField, null, null);
        });
    }
}
