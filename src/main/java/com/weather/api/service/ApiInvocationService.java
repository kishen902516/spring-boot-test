package com.weather.api.service;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.support.DefaultExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class ApiInvocationService {
    
    private static final Logger logger = LoggerFactory.getLogger(ApiInvocationService.class);
    
    private final ProducerTemplate producerTemplate;
    private final CamelContext camelContext;
    
    public ApiInvocationService(ProducerTemplate producerTemplate, CamelContext camelContext) {
        this.producerTemplate = producerTemplate;
        this.camelContext = camelContext;
    }
    
    public Object invokeApi(String url, String httpMethod, Object requestBody, Map<String, Object> headers) {
        logger.info("Invoking API: {} {}", httpMethod, url);
        
        Exchange exchange = new DefaultExchange(camelContext);
        
        // Set the URL and HTTP method
        exchange.getIn().setHeader(Exchange.HTTP_URI, url);
        exchange.getIn().setHeader(Exchange.HTTP_METHOD, httpMethod);
        
        // Set custom headers
        if (headers != null) {
            exchange.setProperty("customHeaders", headers);
        }
        
        // Set request body if provided
        if (requestBody != null) {
            exchange.getIn().setBody(requestBody);
        }
        
        // Send to the Camel route
        Exchange result = producerTemplate.send("direct:invokeApi", exchange);
        
        // Check for exceptions
        if (result.getException() != null) {
            logger.error("API invocation failed", result.getException());
            throw new RuntimeException("API invocation failed: " + result.getException().getMessage(), 
                                     result.getException());
        }
        
        return result.getIn().getBody();
    }
    
    public CompletableFuture<Object> invokeApiAsync(String url, String httpMethod, 
                                                   Object requestBody, Map<String, Object> headers) {
        return CompletableFuture.supplyAsync(() -> invokeApi(url, httpMethod, requestBody, headers));
    }
    
    // Convenience methods for common HTTP methods
    public Object get(String url) {
        return invokeApi(url, "GET", null, null);
    }
    
    public Object get(String url, Map<String, Object> headers) {
        return invokeApi(url, "GET", null, headers);
    }
    
    public Object post(String url, Object requestBody) {
        return invokeApi(url, "POST", requestBody, null);
    }
    
    public Object post(String url, Object requestBody, Map<String, Object> headers) {
        return invokeApi(url, "POST", requestBody, headers);
    }
    
    public Object put(String url, Object requestBody) {
        return invokeApi(url, "PUT", requestBody, null);
    }
    
    public Object put(String url, Object requestBody, Map<String, Object> headers) {
        return invokeApi(url, "PUT", requestBody, headers);
    }
    
    public Object delete(String url) {
        return invokeApi(url, "DELETE", null, null);
    }
    
    public Object delete(String url, Map<String, Object> headers) {
        return invokeApi(url, "DELETE", null, headers);
    }
}