package org.group45.choreday.services;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;

import org.group45.choreday.models.WeatherResponse;

public class WeatherService {

    private static String createConnection(String locationApiUrl) {
        try {
            URL url = URI.create(locationApiUrl).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() != 200) {
                return null;
            }

            Scanner scanner = new Scanner(url.openStream());
            StringBuilder response = new StringBuilder();
            while (scanner.hasNext()) {
                response.append(scanner.nextLine());
            }
            scanner.close();
            return response.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public WeatherResponse getWeatherData(String city) {
        try {
            // 1. Get location coordinates
            String geocodingUrl = "https://geocoding-api.open-meteo.com/v1/search?name=" + city
                    + "&count=1&language=en&format=json";
            String geoResponseStr = createConnection(geocodingUrl);
            
            if (geoResponseStr == null) return null;

            // Convert String to JSON
            JSONObject geoJson = new JSONObject(geoResponseStr);
            if (!geoJson.has("results")) return null;
            
            JSONArray results = geoJson.getJSONArray("results");
            JSONObject firstLocation = results.getJSONObject(0);
            
            double lat = firstLocation.getDouble("latitude");
            double lon = firstLocation.getDouble("longitude");

            // 2. Get actual weather data
            String weatherUrl = "https://api.open-meteo.com/v1/forecast?latitude=" + lat + "&longitude="
                    + lon + "&current=temperature_2m,wind_speed_10m,relative_humidity_2m,apparent_temperature,uv_index,weather_code&format=json";
            String weatherResStr = createConnection(weatherUrl);
            
            if (weatherResStr == null) return null;

            // Convert String to JSON
            JSONObject weatherJson = new JSONObject(weatherResStr);
            JSONObject current = weatherJson.getJSONObject("current");

            // 3. Build our model
            return WeatherResponse.builder()
                    .city(firstLocation.getString("name"))
                    .country(firstLocation.optString("country", "N/A"))
                    .temperature(String.valueOf(current.getDouble("temperature_2m")))
                    .windSpeed(String.valueOf(current.getDouble("wind_speed_10m")))
                    .humidity(String.valueOf(current.getInt("relative_humidity_2m")))
                    .feelsLike(String.valueOf(current.getDouble("apparent_temperature")))
                    .uvIndex(String.valueOf(current.getDouble("uv_index")))
                    .weatherDescription(getWeatherDesc(current.optInt("weather_code", 0)))
                    .latitude(String.valueOf(lat))
                    .longitude(String.valueOf(lon))
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getWeatherDesc(int code) {
        if (code == 0) return "Clear Sky";
        if (code <= 3) return "Partly Cloudy";
        if (code <= 48) return "Foggy";
        if (code <= 57) return "Drizzle";
        if (code <= 67) return "Rainy";
        if (code <= 77) return "Snowy";
        if (code <= 82) return "Rain Showers";
        return "Stormy";
    }
}
