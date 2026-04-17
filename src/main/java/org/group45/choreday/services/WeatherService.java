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
        return getWeatherData(city, null);
    }

    public WeatherResponse getWeatherData(String city, java.time.LocalDateTime targetTime) {
        try {
            // 1. Get location coordinates
            String geocodingUrl = "https://geocoding-api.open-meteo.com/v1/search?name=" + city
                    + "&count=1&language=en&format=json";
            String geoResponseStr = createConnection(geocodingUrl);

            if (geoResponseStr == null)
                return null;

            JSONObject geoJson = new JSONObject(geoResponseStr);
            if (!geoJson.has("results"))
                return null;

            JSONArray results = geoJson.getJSONArray("results");
            JSONObject firstLocation = results.getJSONObject(0);

            double lat = firstLocation.getDouble("latitude");
            double lon = firstLocation.getDouble("longitude");

            // 2. Get actual weather data
            String weatherUrl;
            if (targetTime == null) {
                // Current weather
                weatherUrl = "https://api.open-meteo.com/v1/forecast?latitude=" + lat + "&longitude=" + lon
                        + "&current=temperature_2m,wind_speed_10m,relative_humidity_2m,apparent_temperature,uv_index,weather_code&format=json";
            } else {
                // Future/Forecast weather
                weatherUrl = "https://api.open-meteo.com/v1/forecast?latitude=" + lat + "&longitude=" + lon
                        + "&hourly=temperature_2m,wind_speed_10m,relative_humidity_2m,apparent_temperature,uv_index,weather_code&format=json";
            }

            String weatherResStr = createConnection(weatherUrl);
            if (weatherResStr == null)
                return null;

            JSONObject weatherJson = new JSONObject(weatherResStr);
            JSONObject dataToUse;

            if (targetTime == null) {
                dataToUse = weatherJson.getJSONObject("current");
            } else {
                // Find closest hour in forecast
                JSONObject hourly = weatherJson.getJSONObject("hourly");
                JSONArray times = hourly.getJSONArray("time");
                String targetTimeStr = targetTime.withMinute(0).withSecond(0).withNano(0).toString().replace("T", " ");
                // Open-Meteo uses T format: "2026-03-20T10:00"
                targetTimeStr = targetTime.withMinute(0).withSecond(0).withNano(0).toString().substring(0, 16);

                int index = -1;
                for (int i = 0; i < times.length(); i++) {
                    if (times.getString(i).equals(targetTimeStr)) {
                        index = i;
                        break;
                    }
                }

                if (index == -1)
                    index = 0; // Fallback to first hour if out of range

                dataToUse = new JSONObject();
                dataToUse.put("temperature_2m", hourly.getJSONArray("temperature_2m").get(index));
                dataToUse.put("wind_speed_10m", hourly.getJSONArray("wind_speed_10m").get(index));
                dataToUse.put("relative_humidity_2m", hourly.getJSONArray("relative_humidity_2m").get(index));
                dataToUse.put("apparent_temperature", hourly.getJSONArray("apparent_temperature").get(index));
                dataToUse.put("uv_index", hourly.getJSONArray("uv_index").get(index));
                dataToUse.put("weather_code", hourly.getJSONArray("weather_code").get(index));
            }

            // 3. Build our model
            return WeatherResponse.builder()
                    .city(firstLocation.getString("name"))
                    .country(firstLocation.optString("country", "N/A"))
                    .temperature(String.valueOf(dataToUse.get("temperature_2m")))
                    .windSpeed(String.valueOf(dataToUse.get("wind_speed_10m")))
                    .humidity(String.valueOf(dataToUse.get("relative_humidity_2m")))
                    .feelsLike(String.valueOf(dataToUse.get("apparent_temperature")))
                    .uvIndex(String.valueOf(dataToUse.get("uv_index")))
                    .weatherDescription(getWeatherDesc(dataToUse.optInt("weather_code", 0)))
                    .latitude(String.valueOf(lat))
                    .longitude(String.valueOf(lon))
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getWeatherDesc(int code) {
        switch (code) {
            case 0:
                return "Sunny / Clear Sky";
            case 1:
            case 2:
            case 3:
                return "Partly Cloudy";
            case 45:
            case 48:
                return "Foggy";
            case 51:
            case 53:
            case 55:
                return "Drizzle";
            case 56:
            case 57:
                return "Freezing Drizzle";
            case 61:
                return "Light Rain";
            case 63:
                return "Moderate Rain";
            case 65:
                return "Heavy Rain";
            case 66:
            case 67:
                return "Freezing Rain";
            case 71:
            case 73:
            case 75:
                return "Snowy";
            case 77:
                return "Snow Grains";
            case 80:
            case 81:
            case 82:
                return "Rain Showers";
            case 85:
            case 86:
                return "Snow Showers";
            case 95:
            case 96:
            case 99:
                return "Thunderstorm";
            default:
                return "Unknown";
        }
    }
}
