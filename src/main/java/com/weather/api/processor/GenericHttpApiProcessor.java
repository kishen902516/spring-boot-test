package com.weather.api.processor;

import com.weather.api.service.MsalTokenService;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.http.base.HttpOperationFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class GenericHttpApiProcessor implements Processor {
    
    private static final Logger logger = LoggerFactory.getLogger(GenericHttpApiProcessor.class);
    
    private final MsalTokenService msalTokenService;
    
    public GenericHttpApiProcessor(MsalTokenService msalTokenService) {
        this.msalTokenService = msalTokenService;
    }
    
    @Override
    public void process(Exchange exchange) throws Exception {
        // Get the access token
        String accessToken = msalTokenService.getAccessToken();
        
        // Set Authorization header with Bearer token
        exchange.getIn().setHeader("Authorization", "Bearer " + accessToken);
        
        // Set Content-Type if not already set
        if (exchange.getIn().getHeader("Content-Type") == null) {
            exchange.getIn().setHeader("Content-Type", "application/json");
        }
        
        // Log request details
        String httpMethod = exchange.getIn().getHeader(Exchange.HTTP_METHOD, String.class);
        String httpUri = exchange.getIn().getHeader(Exchange.HTTP_URI, String.class);
        logger.info("Processing {} request to: {}", httpMethod, httpUri);
        
        // Handle any custom headers from exchange properties
        Map<String, Object> customHeaders = exchange.getProperty("customHeaders", Map.class);
        if (customHeaders != null) {
            customHeaders.forEach((key, value) -> {
                exchange.getIn().setHeader(key, value);
                logger.debug("Added custom header: {} = {}", key, value);
            });
        }
        
        // Set up error handling
        exchange.setProperty("CamelExceptionCaught", null);
    }
    
    public void handleApiError(Exchange exchange) {
        Exception exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
        
        if (exception instanceof HttpOperationFailedException) {
            HttpOperationFailedException httpException = (HttpOperationFailedException) exception;
            int statusCode = httpException.getStatusCode();
            String responseBody = httpException.getResponseBody();
            
            logger.error("API call failed with status {}: {}", statusCode, responseBody);
            
            // If 401 Unauthorized, clear token cache and retry
            if (statusCode == 401) {
                logger.info("Received 401 Unauthorized, clearing token cache");
                msalTokenService.clearCache();
                exchange.setProperty("retryRequest", true);
            }
            
            // Set error details in exchange
            exchange.getIn().setHeader("errorStatusCode", statusCode);
            exchange.getIn().setBody(responseBody);
        } else {
            logger.error("Unexpected error during API call", exception);
            exchange.getIn().setHeader("errorMessage", exception.getMessage());
        }
    }
}