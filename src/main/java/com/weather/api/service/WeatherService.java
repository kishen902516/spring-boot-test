package com.weather.api.service;

import com.weather.api.config.WeatherApiConfig;
import com.weather.api.model.WeatherData;
import com.weather.api.model.WeatherResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class WeatherService {
    
    private final WebClient webClient;
    private final WeatherApiConfig config;
    
    @Autowired
    public WeatherService(WeatherApiConfig config) {
        this.config = config;
        this.webClient = WebClient.builder()
                .baseUrl(config.getBaseUrl())
                .build();
    }
    
    public Mono<WeatherData> getWeatherByCity(String city) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/weather")
                        .queryParam("q", city)
                        .queryParam("appid", config.getKey())
                        .queryParam("units", config.getUnits())
                        .build())
                .retrieve()
                .bodyToMono(WeatherResponse.class)
                .map(this::mapToWeatherData)
                .onErrorResume(ex -> Mono.error(new RuntimeException("Failed to fetch weather data for city: " + city, ex)));
    }
    
    public Mono<WeatherData> getWeatherByCoordinates(double lat, double lon) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/weather")
                        .queryParam("lat", lat)
                        .queryParam("lon", lon)
                        .queryParam("appid", config.getKey())
                        .queryParam("units", config.getUnits())
                        .build())
                .retrieve()
                .bodyToMono(WeatherResponse.class)
                .map(this::mapToWeatherData)
                .onErrorResume(ex -> Mono.error(new RuntimeException("Failed to fetch weather data for coordinates: " + lat + "," + lon, ex)));
    }
    
    private WeatherData mapToWeatherData(WeatherResponse response) {
        WeatherData weatherData = new WeatherData();
        weatherData.setCity(response.getName());
        
        if (response.getMain() != null) {
            weatherData.setTemperature(response.getMain().getTemp());
            weatherData.setFeelsLike(response.getMain().getFeelsLike());
            weatherData.setHumidity(response.getMain().getHumidity());
        }
        
        if (response.getWeather() != null && response.getWeather().length > 0) {
            weatherData.setMain(response.getWeather()[0].getMain());
            weatherData.setDescription(response.getWeather()[0].getDescription());
        }
        
        return weatherData;
    }
}