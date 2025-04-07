package com.technology309.eventmanager.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeatherResponse {
    private Main main;
    private Weather[] weather;
    private String name;

    @Data
    public static class Main {
        private double temp;
        private double feels_like;
        private int humidity;
        private double wind_speed;
    }

    @Data
    public static class Weather {
        private String description;
        private String icon;
    }
} 