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
    private DatePicker datePicker;
    @FXML
    private ComboBox<String> hourCombo;
    @FXML
    private ComboBox<String> minuteCombo;
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

        // 3. Setup Time Dropdowns
        for (int i = 0; i < 24; i++) {
            hourCombo.getItems().add(String.format("%02d", i));
        }
        for (int i = 0; i < 60; i += 5) {
            minuteCombo.getItems().add(String.format("%02d", i));
        }
        hourCombo.getSelectionModel().select("09");
        minuteCombo.getSelectionModel().select("00");

        // 4. Setup Table
        activityColumn.setCellValueFactory(new PropertyValueFactory<>("activityName"));
        cityColumn.setCellValueFactory(new PropertyValueFactory<>("city"));
        weatherColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getWeather() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getWeather().getWeatherDescription());
            }
            return new javafx.beans.property.SimpleStringProperty("N/A");
        });
        tempColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getWeather() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getWeather().getTemperature() + "°C");
            }
            return new javafx.beans.property.SimpleStringProperty("N/A");
        });
        timeColumn.setCellValueFactory(cellData -> {
            LocalDateTime dt = cellData.getValue().getScheduledAt();
            if (dt == null)
                dt = cellData.getValue().getCreatedAt();

            if (dt != null) {
                return new javafx.beans.property.SimpleStringProperty(
                        dt.format(DateTimeFormatter.ofPattern("MMM d, h:mm a")));
            }
            return new javafx.beans.property.SimpleStringProperty("Just now");
        });
        activityTable.setItems(choresList);
        loadChores();

        // 5. Initial Weather
        loadCurrentWeather("Accra");

        // 6. Button Actions
        addButton.setOnAction(event -> addChore());
        resetWeatherButton.setOnAction(event -> {
            if (currentRealTimeWeather != null) {
                applyWeatherToUI(currentRealTimeWeather);
                resetWeatherButton.setVisible(false);
            }
        });

        // 7. Table Selection
        activityTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null && newSelection.getWeather() != null) {
                applyWeatherRecordToUI(newSelection.getWeather());
                resetWeatherButton.setVisible(true);
            }
        });

        // 8. Profile Icon (Logout)
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

    private void addChore() {
        String activity = activityField.getText().trim();
        String city = cityField.getText().trim();
        UserModel user = SessionManager.getCurrentUser();

        // Schedule logic
        java.time.LocalDate date = datePicker.getValue();
        String hour = hourCombo.getValue();
        String minute = minuteCombo.getValue();
        java.time.LocalDateTime targetTime = null;

        if (date != null && hour != null && minute != null) {
            try {
                java.time.LocalTime time = java.time.LocalTime.of(Integer.parseInt(hour), Integer.parseInt(minute));
                targetTime = java.time.LocalDateTime.of(date, time);
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "✗ Error", "Failed to parse time.");
                return;
            }
        }

        // Validation
        if (activity.isEmpty() || city.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "⚠ Missing Information",
                    "Please enter both:\n• Activity/Chore Name\n• City");
            return;
        }

        if (user == null) {
            showAlert(Alert.AlertType.ERROR, "✗ Session Error", "Please sign in again.");
            return;
        }

        addLoading.setVisible(true);
        addButton.setDisable(true);

        java.time.LocalDateTime finalTargetTime = targetTime;
        new Thread(() -> {
            boolean success = choreService.saveChore(activity, city, user.getStudentId(), finalTargetTime);
            Platform.runLater(() -> {
                addLoading.setVisible(false);
                addButton.setDisable(false);
                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "✓ Chore Added!", "Chore saved successfully.");
                    activityField.clear();
                    cityField.clear();
                    datePicker.setValue(null);
                    hourCombo.getSelectionModel().select("09");
                    minuteCombo.getSelectionModel().select("00");
                    loadChores();
                } else {
                    showAlert(Alert.AlertType.ERROR, "✗ Error", "Check city name or connection.");
                }
            });
        }).start();
    }

    private void loadChores() {
        UserModel user = SessionManager.getCurrentUser();
        if (user == null)
            return;
        new Thread(() -> {
            List<ChoreModel> chores = choreService.getChoresForUser(user.getStudentId());
            Platform.runLater(() -> choresList.setAll(chores));
        }).start();
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
        conditionAndCountryText.setText(
                weather.getWeatherDescription() + " (Archived), " + weather.getCity() + " " + weather.getCountry());
        humidityText.setText(weather.getHumidity() + "%");
        windText.setText(weather.getWindSpeed() + "km/h");
        uvText.setText(weather.getUvIndex());
        feelsLike.setText(weather.getFeelsLike() + "°C");
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

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.getDialogPane().getStylesheets()
                .add(getClass().getResource("/org/group45/choreday/style.css").toExternalForm());
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
