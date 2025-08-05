package com.weather.api.controller;

import com.weather.api.service.ApiInvocationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class ApiTestController {
    
    private final ApiInvocationService apiInvocationService;
    
    public ApiTestController(ApiInvocationService apiInvocationService) {
        this.apiInvocationService = apiInvocationService;
    }
    
    @GetMapping("/invoke")
    public ResponseEntity<?> testApiInvocation(
            @RequestParam String url,
            @RequestParam(defaultValue = "GET") String method,
            @RequestHeader(required = false) Map<String, String> headers) {
        
        try {
            // Convert headers to Map<String, Object>
            Map<String, Object> headerMap = new HashMap<>();
            if (headers != null) {
                headerMap.putAll(headers);
            }
            
            Object result = apiInvocationService.invokeApi(url, method, null, headerMap);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error invoking API: " + e.getMessage());
        }
    }
    
    @PostMapping("/invoke")
    public ResponseEntity<?> testApiInvocationWithBody(
            @RequestParam String url,
            @RequestBody Object requestBody,
            @RequestHeader(required = false) Map<String, String> headers) {
        
        try {
            // Convert headers to Map<String, Object>
            Map<String, Object> headerMap = new HashMap<>();
            if (headers != null) {
                headerMap.putAll(headers);
            }
            
            Object result = apiInvocationService.post(url, requestBody, headerMap);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error invoking API: " + e.getMessage());
        }
    }
}