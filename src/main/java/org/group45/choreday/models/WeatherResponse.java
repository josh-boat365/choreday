package org.group45.choreday.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeatherResponse {
    private String temperature;
    private String weatherDescription;
    private String windSpeed;
    private String humidity;
    private String feelsLike;
    private String uvIndex;
    private String city;
    private String country;
    private String latitude;
    private String longitude;
}