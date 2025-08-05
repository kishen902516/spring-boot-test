package com.weather.api.controller;

import com.weather.api.model.WeatherData;
import com.weather.api.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/weather")
@CrossOrigin(origins = "*")
public class WeatherController {
    
    private final WeatherService weatherService;
    
    @Autowired
    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }
    
    @GetMapping("/city/{city}")
    public Mono<ResponseEntity<WeatherData>> getWeatherByCity(@PathVariable String city) {
        return weatherService.getWeatherByCity(city)
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.badRequest().build());
    }
    
    @GetMapping("/coordinates")
    public Mono<ResponseEntity<WeatherData>> getWeatherByCoordinates(
            @RequestParam double lat, 
            @RequestParam double lon) {
        return weatherService.getWeatherByCoordinates(lat, lon)
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Weather API is running!");
    }
    
    @GetMapping("/test/{city}")
    public Mono<String> testWeatherApi(@PathVariable String city) {
        return weatherService.getWeatherByCity(city)
                .map(weather -> "Success: " + weather.getCity() + " - " + weather.getTemperature() + "°C")
                .onErrorReturn("Error: Failed to fetch weather data. Check API key and city name.");
    }
}