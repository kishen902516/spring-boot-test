package com.weather.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WeatherResponse {
    
    private String name;
    private Main main;
    private Weather[] weather;
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Main getMain() {
        return main;
    }
    
    public void setMain(Main main) {
        this.main = main;
    }
    
    public Weather[] getWeather() {
        return weather;
    }
    
    public void setWeather(Weather[] weather) {
        this.weather = weather;
    }
    
    public static class Main {
        private double temp;
        @JsonProperty("feels_like")
        private double feelsLike;
        private int humidity;
        
        public double getTemp() {
            return temp;
        }
        
        public void setTemp(double temp) {
            this.temp = temp;
        }
        
        public double getFeelsLike() {
            return feelsLike;
        }
        
        public void setFeelsLike(double feelsLike) {
            this.feelsLike = feelsLike;
        }
        
        public int getHumidity() {
            return humidity;
        }
        
        public void setHumidity(int humidity) {
            this.humidity = humidity;
        }
    }
    
    public static class Weather {
        private String main;
        private String description;
        
        public String getMain() {
            return main;
        }
        
        public void setMain(String main) {
            this.main = main;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
    }
}