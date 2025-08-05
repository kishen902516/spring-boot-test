package com.weather.api.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.microsoft.aad.msal4j.*;
import com.weather.api.config.auth.MsalConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class MsalTokenService {
    
    private static final Logger logger = LoggerFactory.getLogger(MsalTokenService.class);
    private static final String CACHE_KEY = "bearer_token";
    
    private final MsalConfiguration msalConfiguration;
    private final Cache<String, String> tokenCache;
    private ConfidentialClientApplication clientApplication;
    
    public MsalTokenService(MsalConfiguration msalConfiguration) {
        this.msalConfiguration = msalConfiguration;
        this.tokenCache = Caffeine.newBuilder()
                .expireAfterWrite(55, TimeUnit.MINUTES) // Tokens typically expire in 60 minutes
                .maximumSize(10)
                .build();
        
        initializeClientApplication();
    }
    
    private void initializeClientApplication() {
        try {
            IClientCredential credential = ClientCredentialFactory.createFromSecret(msalConfiguration.getClientSecret());
            
            clientApplication = ConfidentialClientApplication.builder(
                    msalConfiguration.getClientId(),
                    credential)
                    .authority(msalConfiguration.getAuthority())
                    .build();
                    
        } catch (MalformedURLException e) {
            logger.error("Failed to initialize MSAL client application", e);
            throw new RuntimeException("Failed to initialize MSAL client application", e);
        }
    }
    
    public String getAccessToken() {
        // Check cache first
        String cachedToken = tokenCache.getIfPresent(CACHE_KEY);
        if (cachedToken != null) {
            logger.debug("Returning cached token");
            return cachedToken;
        }
        
        // If not in cache, acquire new token
        logger.info("Acquiring new access token from Azure AD");
        try {
            ClientCredentialParameters parameters = ClientCredentialParameters.builder(
                    Collections.singleton(msalConfiguration.getScope()))
                    .build();
            
            CompletableFuture<IAuthenticationResult> future = clientApplication.acquireToken(parameters);
            IAuthenticationResult result = future.get();
            
            String accessToken = result.accessToken();
            
            // Cache the token
            tokenCache.put(CACHE_KEY, accessToken);
            logger.info("Successfully acquired and cached new access token");
            
            return accessToken;
            
        } catch (Exception e) {
            logger.error("Failed to acquire access token", e);
            throw new RuntimeException("Failed to acquire access token", e);
        }
    }
    
    public void clearCache() {
        tokenCache.invalidateAll();
        logger.info("Token cache cleared");
    }
}