package org.btuk.http.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.btuk.http.auth.ApiCredentials;
import org.btuk.http.auth.Authentication;
import org.btuk.http.auth.BearerTokenAuthentication;
import org.btuk.http.auth.TokenManager;
import org.btuk.http.dto.ErrorResponse;
import org.btuk.http.exception.BTUKApiException;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class BTUKHttpClient {
    private final String baseUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final Authentication authentication;

    /**
     * Creates a new BTUKHttpClient with the specified base URL and authentication strategy.
     */
    public BTUKHttpClient(String baseUrl, Authentication authentication) {
        this(baseUrl, authentication, HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build(), new ObjectMapper());
    }

    /**
     * Creates a new BTUKHttpClient with the specified base URL, authentication strategy, custom HttpClient and ObjectMapper.
     */
    public BTUKHttpClient(String baseUrl, Authentication authentication, HttpClient httpClient, ObjectMapper objectMapper) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.authentication = authentication;
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    /**
     * Creates a new BTUKHttpClient with Bearer token authentication using the provided credentials.
     */
    public BTUKHttpClient(String baseUrl, ApiCredentials credentials) {
        this(baseUrl, "/api/auth/client/token", credentials);
    }

    /**
     * Creates a new BTUKHttpClient with Bearer token authentication using the provided credentials and custom token path.
     */
    public BTUKHttpClient(String baseUrl, String tokenPath, ApiCredentials credentials) {
        this(baseUrl, tokenPath, credentials, HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build(), new ObjectMapper());
    }

    /**
     * Creates a new BTUKHttpClient with Bearer token authentication using the provided credentials, custom token path, HttpClient and ObjectMapper.
     */
    public BTUKHttpClient(String baseUrl, String tokenPath, ApiCredentials credentials, HttpClient httpClient, ObjectMapper objectMapper) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.authentication = new BearerTokenAuthentication(new TokenManager(this.baseUrl, tokenPath, credentials, httpClient, objectMapper));
    }

    public <T> CompletableFuture<T> get(String path, Class<T> responseType) {
        return sendRequest("GET", path, null, responseType);
    }

    public <T> CompletableFuture<T> post(String path, Object body, Class<T> responseType) {
        return sendRequest("POST", path, body, responseType);
    }

    public <T> CompletableFuture<T> put(String path, Object body, Class<T> responseType) {
        return sendRequest("PUT", path, body, responseType);
    }

    public CompletableFuture<Void> delete(String path) {
        return sendRequest("DELETE", path, null, Void.class);
    }

    public <T> CompletableFuture<T> send(HttpRequestSpec<T> spec) {
        String path = spec.getPath();
        if (spec.getQueryParameters() != null && !spec.getQueryParameters().isEmpty()) {
            StringBuilder sb = new StringBuilder(path);
            sb.append(path.contains("?") ? "&" : "?");
            spec.getQueryParameters().forEach((k, v) -> {
                sb.append(URLEncoder.encode(k, StandardCharsets.UTF_8))
                        .append("=")
                        .append(URLEncoder.encode(v, StandardCharsets.UTF_8))
                        .append("&");
            });
            path = sb.substring(0, sb.length() - 1);
        }
        return sendRequest(spec.getMethod(), path, spec.getBody(), spec.getResponseType());
    }

    private <T> CompletableFuture<T> sendRequest(String method, String path, Object body, Class<T> responseType) {
        try {
            String sanitizedPath = path.startsWith("/") ? path : "/" + path;
            String fullUrl = baseUrl + sanitizedPath;
            // Basic double slash prevention if baseUrl ended with / (though we handle it in constructor)
            fullUrl = fullUrl.replaceAll("(?<!:)/{2,}", "/"); 

            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(fullUrl))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json");

            CompletableFuture<HttpRequest.Builder> authFuture = authentication != null
                    ? authentication.apply(requestBuilder)
                    : CompletableFuture.completedFuture(requestBuilder);

            return authFuture.thenCompose(builder -> {
                try {
                    HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.noBody();
                    if (body != null) {
                        bodyPublisher = HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body));
                    }

                    HttpRequest request = builder.method(method, bodyPublisher).build();

                    return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                            .thenApply(response -> {
                                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                                    if (responseType == Void.class || response.body().isEmpty()) {
                                        return null;
                                    }
                                    try {
                                        return objectMapper.readValue(response.body(), responseType);
                                    } catch (Exception e) {
                                        throw new BTUKApiException(response.statusCode(), "Failed to parse successful response body", null);
                                    }
                                } else {
                                    ErrorResponse errorResponse = null;
                                    String message = "API request failed with status " + response.statusCode();
                                    if (!response.body().isEmpty()) {
                                        try {
                                            errorResponse = objectMapper.readValue(response.body(), ErrorResponse.class);
                                            if (errorResponse != null && errorResponse.getErrorCode() != null) {
                                                message = "API error: " + errorResponse.getErrorCode();
                                            }
                                        } catch (Exception ignored) {
                                            // Body is not a valid ErrorResponse, use default message
                                            message += ": " + response.body();
                                        }
                                    }
                                    throw new BTUKApiException(response.statusCode(), message, errorResponse);
                                }
                            });
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    public HttpClient getInternalClient() {
        return httpClient;
    }

    public ObjectMapper getInternalObjectMapper() {
        return objectMapper;
    }
}
