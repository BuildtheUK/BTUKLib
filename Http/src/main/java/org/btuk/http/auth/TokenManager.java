package org.btuk.http.auth;

import org.btuk.http.dto.TokenResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class TokenManager {
    private final String baseUrl;
    private final String tokenPath;
    private final ApiCredentials credentials;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    private String token;
    private Instant expiration;
    private CompletableFuture<String> inFlightRefresh;

    public TokenManager(String baseUrl, ApiCredentials credentials, HttpClient httpClient, ObjectMapper objectMapper) {
        this(baseUrl, "/api/auth/client/token", credentials, httpClient, objectMapper);
    }

    public TokenManager(String baseUrl, String tokenPath, ApiCredentials credentials, HttpClient httpClient, ObjectMapper objectMapper) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.tokenPath = tokenPath.startsWith("/") ? tokenPath : "/" + tokenPath;
        this.credentials = credentials;
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    public synchronized CompletableFuture<String> getToken() {
        if (token != null && expiration != null && Instant.now().isBefore(expiration.minusSeconds(60))) {
            return CompletableFuture.completedFuture(token);
        }

        if (inFlightRefresh != null && !inFlightRefresh.isCompletedExceptionally()) {
            return inFlightRefresh;
        }

        inFlightRefresh = refreshToken().whenComplete((res, ex) -> {
            synchronized (this) {
                inFlightRefresh = null;
            }
        });
        return inFlightRefresh;
    }

    private CompletableFuture<String> refreshToken() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + tokenPath))
                .header("X-API-CLIENT-ID", credentials.getClientId().toString())
                .header("X-API-KEY-ID", credentials.getKeyId().toString())
                .header("X-API-KEY", credentials.getSecret())
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != 200) {
                        throw new RuntimeException("Failed to obtain token: " + response.statusCode() + " " + response.body());
                    }
                    try {
                        TokenResponse tokenResponse = objectMapper.readValue(response.body(), TokenResponse.class);
                        updateToken(tokenResponse);
                        return token;
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to parse token response", e);
                    }
                });
    }

    private void updateToken(TokenResponse tokenResponse) {
        this.token = tokenResponse.getToken();
        if (tokenResponse.getExpiresAtEpochSeconds() != null) {
            this.expiration = Instant.ofEpochSecond(tokenResponse.getExpiresAtEpochSeconds());
        } else {
            this.expiration = extractExpiration(this.token);
        }
    }

    private Instant extractExpiration(String jwt) {
        try {
            String[] parts = jwt.split("\\.");
            if (parts.length < 2) return null;
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            Map<String, Object> claims = objectMapper.readValue(payload, Map.class);
            Object exp = claims.get("exp");
            if (exp instanceof Number) {
                return Instant.ofEpochSecond(((Number) exp).longValue());
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
