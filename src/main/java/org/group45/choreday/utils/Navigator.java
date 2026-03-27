package org.group45.choreday.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public class Navigator {
    public static void navigateTo(String fxmlFile, Node sourceNode, Integer width, Integer height) {
        int widgetWidth = Optional.ofNullable(width).orElse(800);
        int widgetHeight = Optional.ofNullable(height).orElse(600);
        try {
            // 1. Load the FXML file for the next screen (using absolute path because Navigator is in utils package)
            FXMLLoader fxmlLoader = new FXMLLoader(Navigator.class.getResource("/org/group45/choreday/" + fxmlFile));
            Parent root = fxmlLoader.load();

            // 2. Create the new scene
            Scene scene = new Scene(root, widgetWidth, widgetHeight);

            // 3. Get the current stage (window) from the button click event
            Stage stage = (Stage) sourceNode.getScene().getWindow();

            // 4. Set the new scene to the stage and show it
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
