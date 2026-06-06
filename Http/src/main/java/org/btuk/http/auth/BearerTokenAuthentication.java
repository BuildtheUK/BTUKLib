package org.btuk.http.auth;

import java.net.http.HttpRequest;
import java.util.concurrent.CompletableFuture;

/**
 * An authentication strategy that uses a Bearer token managed by a TokenManager.
 */
public class BearerTokenAuthentication implements Authentication {
    private final TokenManager tokenManager;

    public BearerTokenAuthentication(TokenManager tokenManager) {
        this.tokenManager = tokenManager;
    }

    public BearerTokenAuthentication(String baseUrl, ApiCredentials credentials, java.net.http.HttpClient httpClient, com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
        this(new TokenManager(baseUrl, credentials, httpClient, objectMapper));
    }

    public BearerTokenAuthentication(String baseUrl, String tokenPath, ApiCredentials credentials, java.net.http.HttpClient httpClient, com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
        this(new TokenManager(baseUrl, tokenPath, credentials, httpClient, objectMapper));
    }

    @Override
    public CompletableFuture<HttpRequest.Builder> apply(HttpRequest.Builder requestBuilder) {
        return tokenManager.getToken().thenApply(token -> 
            requestBuilder.header("Authorization", "Bearer " + token)
        );
    }
}
