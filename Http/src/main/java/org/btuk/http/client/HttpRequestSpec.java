package org.btuk.http.client;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.util.Map;

/**
 * Represents an HTTP request specification.
 * @param <T> The type of the response.
 */
@Getter
@Builder
public class HttpRequestSpec<T> {
    private final String method;
    private final String path;
    private final Object body;
    private final Class<T> responseType;
    @Singular
    private final Map<String, String> queryParameters;

    public static class HttpRequestSpecBuilder<T> {
        private Map<String, Object> bodyMap;

        public HttpRequestSpecBuilder<T> bodyParameter(String key, Object value) {
            if (this.body != null && bodyMap == null) {
                if (this.body instanceof Map) {
                    bodyMap = new java.util.HashMap<>((Map<String, Object>) this.body);
                    this.body = bodyMap;
                } else {
                    throw new IllegalStateException("Cannot add body parameter when a non-Map body object is already set");
                }
            }
            if (bodyMap == null) {
                bodyMap = new java.util.HashMap<>();
                this.body = bodyMap;
            }
            bodyMap.put(key, value);
            return this;
        }
    }

    /**
     * Creates a GET request specification.
     */
    public static <T> HttpRequestSpec<T> get(String path, Class<T> responseType) {
        return HttpRequestSpec.<T>builder()
                .method("GET")
                .path(path)
                .responseType(responseType)
                .build();
    }

    /**
     * Creates a POST request specification.
     */
    public static <T> HttpRequestSpec<T> post(String path, Object body, Class<T> responseType) {
        return HttpRequestSpec.<T>builder()
                .method("POST")
                .path(path)
                .body(body)
                .responseType(responseType)
                .build();
    }

    /**
     * Creates a POST request specification with arguments merged into a map.
     */
    public static <T> HttpRequestSpec<T> post(String path, Class<T> responseType, Map<String, Object> args) {
        return HttpRequestSpec.<T>builder()
                .method("POST")
                .path(path)
                .body(args)
                .responseType(responseType)
                .build();
    }

    /**
     * Creates a PUT request specification.
     */
    public static <T> HttpRequestSpec<T> put(String path, Object body, Class<T> responseType) {
        return HttpRequestSpec.<T>builder()
                .method("PUT")
                .path(path)
                .body(body)
                .responseType(responseType)
                .build();
    }

    /**
     * Creates a DELETE request specification.
     */
    public static HttpRequestSpec<Void> delete(String path) {
        return HttpRequestSpec.<Void>builder()
                .method("DELETE")
                .path(path)
                .responseType(Void.class)
                .build();
    }
}
