package com.technology309.eventmanager.service;

import com.technology309.eventmanager.dto.WeatherResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class WeatherService {
    private final RestTemplate restTemplate;
    
    @Value("${WEATHER_API_KEY}")
    private String apiKey;
    
    @Value("${WEATHER_API_BASE_URL}")
    private String baseUrl;

    public String getWeatherData(String location) {
        try {
            String url = String.format("%s?q=%s&appid=%s&units=metric", baseUrl, location, apiKey);
            WeatherResponse response = restTemplate.getForObject(url, WeatherResponse.class);
            
            if (response != null && response.getMain() != null && response.getWeather() != null && response.getWeather().length > 0) {
                return String.format("Temperature: %.1f°C, Feels like: %.1f°C, Humidity: %d%%, Wind: %.1f m/s, Conditions: %s",
                    response.getMain().getTemp(),
                    response.getMain().getFeels_like(),
                    response.getMain().getHumidity(),
                    response.getMain().getWind_speed(),
                    response.getWeather()[0].getDescription());
            }
            return "Weather data unavailable for " + location;
        } catch (Exception e) {
            return "Error fetching weather data for " + location + ": " + e.getMessage();
        }
    }
} 