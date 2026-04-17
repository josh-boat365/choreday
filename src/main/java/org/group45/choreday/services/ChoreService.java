package org.group45.choreday.services;

import org.group45.choreday.models.ChoreModel;
import org.group45.choreday.models.WeatherRecord;
import org.group45.choreday.models.WeatherResponse;
import org.group45.choreday.utils.DatabaseConnectionPool;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChoreService {
    private WeatherService weatherService = new WeatherService();

    public boolean saveChore(String activityName, String city, String studentId) {
        WeatherResponse weather = weatherService.getWeatherData(city);
        if (weather == null) return false;

        try (Connection conn = DatabaseConnectionPool.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Save Weather Record
                String weatherSql = "INSERT INTO weather_records (temperature, wind_speed, humidity, uv_index, feels_like, city, country) VALUES (?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement weatherStmt = conn.prepareStatement(weatherSql, Statement.RETURN_GENERATED_KEYS);
                weatherStmt.setString(1, weather.getTemperature());
                weatherStmt.setString(2, weather.getWindSpeed());
                weatherStmt.setString(3, weather.getHumidity());
                weatherStmt.setString(4, weather.getUvIndex());
                weatherStmt.setString(5, weather.getFeelsLike());
                weatherStmt.setString(6, weather.getCity());
                weatherStmt.setString(7, weather.getCountry());

                weatherStmt.executeUpdate();
                ResultSet rs = weatherStmt.getGeneratedKeys();
                long weatherId = -1;
                if (rs.next()) {
                    weatherId = rs.getLong(1);
                }

                // 2. Save Chore
                String choreSql = "INSERT INTO chores (activity_name, city, weather_id, student_id) VALUES (?, ?, ?, ?)";
                PreparedStatement choreStmt = conn.prepareStatement(choreSql);
                choreStmt.setString(1, activityName);
                choreStmt.setString(2, city);
                choreStmt.setLong(3, weatherId);
                choreStmt.setString(4, studentId);
                choreStmt.executeUpdate();

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<ChoreModel> getChoresForUser(String studentId) {
        List<ChoreModel> chores = new ArrayList<>();
        String sql = "SELECT c.*, w.temperature, w.wind_speed, w.humidity, w.uv_index, w.feels_like, w.city as w_city, w.country " +
                     "FROM chores c LEFT JOIN weather_records w ON c.weather_id = w.id " +
                     "WHERE c.student_id = ? ORDER BY c.created_at DESC";

        try (Connection conn = DatabaseConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, studentId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                WeatherRecord weather = WeatherRecord.builder()
                        .id(rs.getLong("weather_id"))
                        .temperature(rs.getString("temperature"))
                        .windSpeed(rs.getString("wind_speed"))
                        .humidity(rs.getString("humidity"))
                        .uvIndex(rs.getString("uv_index"))
                        .feelsLike(rs.getString("feels_like"))
                        .city(rs.getString("w_city"))
                        .country(rs.getString("country"))
                        .build();

                ChoreModel chore = ChoreModel.builder()
                        .id(rs.getLong("id"))
                        .activityName(rs.getString("activity_name"))
                        .city(rs.getString("city"))
                        .weather(weather)
                        .build();
                
                chores.add(chore);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return chores;
    }
}
