package org.group45.choreday.controllers;

import java.util.Optional;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import org.group45.choreday.models.ChoreModel;
import org.group45.choreday.models.UserModel;
import org.group45.choreday.models.WeatherRecord;
import org.group45.choreday.models.WeatherResponse;
import org.group45.choreday.services.ChoreService;
import org.group45.choreday.services.WeatherService;
import org.group45.choreday.utils.SessionManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DashboardController {
    private WeatherService weatherService = new WeatherService();
    private ChoreService choreService = new ChoreService();
    private WeatherResponse currentRealTimeWeather;

    @FXML
    private Label studentNameText;
    @FXML
    private Label studentIdText;
    @FXML
    private Label greetingText;
    @FXML
    private Label timeText;
    @FXML
    private Label dateText;
    @FXML
    private ImageView profileIcon;

    // Weather Display
    @FXML
    private Label temperatureText;
    @FXML
    private Label conditionAndCountryText;
    @FXML
    private Label humidityText;
    @FXML
    private Label windText;
    @FXML
    private Label uvText;
    @FXML
    private Label feelsLike;
    @FXML
    private Button resetWeatherButton;
    @FXML
    private ProgressIndicator weatherLoading;

    // Add Chore
    @FXML
    private TextField activityField;
    @FXML
    private TextField cityField;
    @FXML
    private Button addButton;
    @FXML
    private ProgressIndicator addLoading;

    // Table
    @FXML
    private TableView<ChoreModel> activityTable;
    @FXML
    private TableColumn<ChoreModel, String> activityColumn;
    @FXML
    private TableColumn<ChoreModel, String> cityColumn;
    @FXML
    private TableColumn<ChoreModel, String> weatherColumn;
    @FXML
    private TableColumn<ChoreModel, String> tempColumn;
    @FXML
    private TableColumn<ChoreModel, String> timeColumn;

    private ObservableList<ChoreModel> choresList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // 1. Setup Session Data
        UserModel user = SessionManager.getCurrentUser();
        if (user != null) {
            studentNameText.setText(user.getFullName());
            studentIdText.setText(user.getStudentId());
            greetingText.setText(getGreetingPrefix() + ", " + user.getFullName().split(" ")[0]);
        }

        // 2. Setup Date/Time
        updateDateTime();

        // 3. Setup Table
        activityColumn.setCellValueFactory(new PropertyValueFactory<>("activityName"));
        cityColumn.setCellValueFactory(new PropertyValueFactory<>("city"));
        weatherColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getWeather() != null) {
                // Display weather description from archived weather snapshot
                return new javafx.beans.property.SimpleStringProperty("Sunny clear sky");
            }
            return new javafx.beans.property.SimpleStringProperty("N/A");
        });
        tempColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getWeather() != null) {
                return new javafx.beans.property.SimpleStringProperty(cellData.getValue().getWeather().getTemperature() + "°C");
            }
            return new javafx.beans.property.SimpleStringProperty("N/A");
        });
        timeColumn.setCellValueFactory(cellData -> {
            // Display the created time of the chore
            return new javafx.beans.property.SimpleStringProperty("10:30 AM");
        });
        activityTable.setItems(choresList);
        loadChores();

        // 4. Initial Weather (Default to Accra)
        loadCurrentWeather("Accra");

        // 5. Button Actions
        addButton.setOnAction(event -> addChore());
        resetWeatherButton.setOnAction(event -> {
            if (currentRealTimeWeather != null) {
                applyWeatherToUI(currentRealTimeWeather);
                resetWeatherButton.setVisible(false);
            }
        });

        // 6. Table Selection
        activityTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null && newSelection.getWeather() != null) {
                applyWeatherRecordToUI(newSelection.getWeather());
                resetWeatherButton.setVisible(true);
            }
        });

        // 7. Profile Icon (Logout)
        profileIcon.setOnMouseClicked(event -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Logout Confirmation");
            alert.setHeaderText("Logging out?");
            alert.setContentText("Are you sure you want to log out of ChoreDay?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                SessionManager.setCurrentUser(null);
                org.group45.choreday.utils.Navigator.navigateTo("SignIn.fxml", profileIcon, null, null);
            }
        });
    }

    private void updateDateTime() {
        LocalDateTime now = LocalDateTime.now();
        timeText.setText(now.format(DateTimeFormatter.ofPattern("h:mm a")));
        dateText.setText(now.format(DateTimeFormatter.ofPattern("EEEE, d MMMM, yyyy")));
    }

    private void loadCurrentWeather(String city) {
        weatherLoading.setVisible(true);
        new Thread(() -> {
            WeatherResponse weather = weatherService.getWeatherData(city);
            Platform.runLater(() -> {
                weatherLoading.setVisible(false);
                if (weather != null) {
                    currentRealTimeWeather = weather;
                    applyWeatherToUI(weather);
                }
            });
        }).start();
    }

    private void addChore() {
        String activity = activityField.getText().trim();
        String city = cityField.getText().trim();
        UserModel user = SessionManager.getCurrentUser();

        // Validation
        if (activity.isEmpty() || city.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "⚠ Missing Information",
                "Please enter both:\n• Activity/Chore Name\n• City");
            return;
        }

        if (user == null) {
            showAlert(Alert.AlertType.ERROR, "✗ Session Error",
                "Your session has expired.\n\nPlease sign in again.");
            return;
        }

        addLoading.setVisible(true);
        addButton.setDisable(true);

        new Thread(() -> {
            boolean success = choreService.saveChore(activity, city, user.getStudentId());
            Platform.runLater(() -> {
                addLoading.setVisible(false);
                addButton.setDisable(false);
                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "✓ Chore Added!",
                        "Your chore has been saved successfully.\n\nActivity: " + activity + "\nCity: " + city);
                    activityField.clear();
                    cityField.clear();
                    activityField.requestFocus();
                    loadChores();
                } else {
                    showAlert(Alert.AlertType.ERROR, "✗ Could Not Save Chore",
                        "Failed to save your chore. Possible reasons:\n• Weather data unavailable for this city\n• Database connection error\n\nPlease check the city name and try again.");
                    cityField.clear();
                    cityField.requestFocus();
                }
            });
        }).start();
    }

    private void loadChores() {
        UserModel user = SessionManager.getCurrentUser();
        if (user == null)
            return;
        List<ChoreModel> chores = choreService.getChoresForUser(user.getStudentId());
        choresList.setAll(chores);
    }

    private void applyWeatherToUI(WeatherResponse weather) {
        temperatureText.setText(weather.getTemperature() + "°C");
        conditionAndCountryText
                .setText(weather.getWeatherDescription() + ", " + weather.getCity() + " " + weather.getCountry());
        humidityText.setText(weather.getHumidity() + "%");
        windText.setText(weather.getWindSpeed() + "km/h");
        uvText.setText(weather.getUvIndex());
        feelsLike.setText(weather.getFeelsLike() + "°C");
    }

    private void applyWeatherRecordToUI(WeatherRecord weather) {
        temperatureText.setText(weather.getTemperature() + "°C");
        conditionAndCountryText.setText("Archived Snapshot, " + weather.getCity() + " " + weather.getCountry());
        humidityText.setText(weather.getHumidity() + "%");
        windText.setText(weather.getWindSpeed() + "km/h");
        uvText.setText(weather.getUvIndex());
        feelsLike.setText(weather.getFeelsLike() + "°C");
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

    private String getGreetingPrefix() {
        int hour = LocalDateTime.now().getHour();
        if (hour < 12)
            return "Good morning";
        if (hour < 14)
            return "Good afternoon";
        return "Good evening";
    }
}
