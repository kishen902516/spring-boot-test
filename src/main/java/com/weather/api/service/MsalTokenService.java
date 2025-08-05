package com.weather.api.service;

import com.microsoft.aad.msal4j.*;
import com.weather.api.config.auth.MsalConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Service
public class MsalTokenService {
    
    private static final Logger logger = LoggerFactory.getLogger(MsalTokenService.class);
    
    private final MsalConfiguration msalConfiguration;
    private ConfidentialClientApplication clientApplication;
    
    public MsalTokenService(MsalConfiguration msalConfiguration) {
        this.msalConfiguration = msalConfiguration;
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
        logger.debug("Attempting to acquire access token");
        
        try {
            Set<String> scopes = Collections.singleton(msalConfiguration.getScope());
            
            // First, try to acquire token silently from cache
            IAuthenticationResult result = acquireTokenSilently(scopes);
            
            if (result == null) {
                // If silent acquisition fails, acquire new token
                logger.info("No cached token found, acquiring new token from Azure AD");
                result = acquireTokenInteractively(scopes);
            } else {
                logger.debug("Successfully acquired token from cache");
            }
            
            return result.accessToken();
            
        } catch (Exception e) {
            logger.error("Failed to acquire access token", e);
            throw new RuntimeException("Failed to acquire access token", e);
        }
    }
    
    private IAuthenticationResult acquireTokenSilently(Set<String> scopes) {
        try {
            // Get accounts from cache
            Set<IAccount> accounts = clientApplication.getAccounts().join();
            
            if (!accounts.isEmpty()) {
                // Use the first account (for client credentials flow, there's typically only one)
                SilentParameters silentParameters = SilentParameters.builder(
                        scopes,
                        accounts.iterator().next())
                        .build();
                
                CompletableFuture<IAuthenticationResult> future = clientApplication.acquireTokenSilently(silentParameters);
                return future.join();
            }
        } catch (Exception e) {
            logger.debug("Silent token acquisition failed, will acquire new token", e);
        }
        
        return null;
    }
    
    private IAuthenticationResult acquireTokenInteractively(Set<String> scopes) throws Exception {
        ClientCredentialParameters parameters = ClientCredentialParameters.builder(scopes)
                .build();
        
        CompletableFuture<IAuthenticationResult> future = clientApplication.acquireToken(parameters);
        IAuthenticationResult result = future.get();
        
        logger.info("Successfully acquired new access token");
        return result;
    }
    
    public void clearCache() {
        // MSAL4J doesn't provide a direct way to clear the entire cache,
        // but we can remove accounts which will force new token acquisition
        try {
            Set<IAccount> accounts = clientApplication.getAccounts().join();
            for (IAccount account : accounts) {
                clientApplication.removeAccount(account).join();
            }
            logger.info("Token cache cleared");
        } catch (Exception e) {
            logger.error("Failed to clear token cache", e);
        }
    }
}