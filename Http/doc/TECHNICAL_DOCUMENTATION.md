# Technical Documentation: Http Module

The `Http` module provides a specialized HTTP client for interacting with the BTUK API, handling authentication and token management automatically.

## Core Components

### 1. `BTUKHttpClient`
The primary entry point for making API calls. It encapsulates an `HttpClient`, an `ObjectMapper`, and an `Authentication` strategy.

- **Constructors**: 
    - `BTUKHttpClient(String baseUrl, Authentication authentication)`: Base constructor.
    - `BTUKHttpClient(String baseUrl, ApiCredentials credentials)`: Convenience constructor for Bearer token authentication (uses default `/api/auth/client/token` path).
    - `BTUKHttpClient(String baseUrl, String tokenPath, ApiCredentials credentials)`: Convenience constructor with a custom token path.
- **Methods**:
    - `send(HttpRequestSpec<T> spec)`: Executes a request based on a static specification.
    - `get(String path, Class<T> responseType)`: Performs an asynchronous GET request.
    - `post(String path, Object body, Class<T> responseType)`: Performs an asynchronous POST request with a JSON body.
    - `put(String path, Object body, Class<T> responseType)`: Performs an asynchronous PUT request with a JSON body.
    - `delete(String path)`: Performs an asynchronous DELETE request.

### 2. `Authentication` (Abstraction)
The client supports different authentication strategies via the `Authentication` interface.
- `NoAuthentication`: For unauthenticated requests.
- `BearerTokenAuthentication`: For JWT-based authentication (uses `TokenManager`).

### 3. `HttpRequestSpec`
A fluent API for defining HTTP requests statically.
- Supports setting methods, paths, bodies, and query parameters.
- Provides helper methods for common REST verbs.
- Provides a fluent `bodyParameter(key, value)` method for map-based bodies.

### 4. `TokenManager`
Responsible for obtaining and refreshing JWT tokens.

- **Configurable Token Path**: The default authentication endpoint is `/api/auth/client/token`, but it can be overridden in the constructor.
- **Automatic Refresh**: It checks the token's expiration (from the JWT payload) before each request. If the token is expired or expires within 60 seconds, it automatically requests a new one using the provided credentials.
- **Thread-Safety**: Token access and refreshing are synchronized to prevent redundant authentication calls.

### 3. `ApiCredentials`
A data holder for authentication credentials:
- `clientId`: UUID (Server identity)
- `keyId`: UUID (Key identity)
- `secret`: String (The secret key)

### 4. Generated DTOs
Most data transfer objects (DTOs) used for requests and responses are automatically generated from the OpenAPI specification (`api.yaml`) using the `openapi-generator-maven-plugin`. These are located in the `org.btuk.http.dto` package.

The generator is configured to only produce models (DTOs) and omit the standard `ApiClient` and API classes, as the project uses a custom `BTUKHttpClient`. This is achieved by setting `supportUrlQuery` to `false` in the plugin configuration, which removes references to `ApiClient` in the generated models.

### 5. Exception Handling: `BTUKApiException`
When a request fails (non-2xx status code), the client throws a `BTUKApiException`.

- **StatusCode**: The HTTP status code returned by the server.
- **ErrorResponse**: If the server returned a JSON error response that matches the `ErrorResponse` DTO, it is parsed and made available.
- **ErrorCode**: Convenient access to the standardized error string (from `ErrorResponse.errorCode`).
- **Parameters**: Optional list of strings providing context for the error (e.g., entity IDs, missing field names) to be used in localized messages.
- **Message**: A descriptive error message, including the `errorCode` from the `ErrorResponse` if present.

## Authentication Flow

1. The client is initialized with an `Authentication` strategy.
2. For `BearerTokenAuthentication`, upon the first request (or when the token is near expiration), `TokenManager` calls `POST /api/auth/client/token` with the `X-API-CLIENT-ID`, `X-API-KEY-ID`, and `X-API-KEY` headers.
3. The server returns a `TokenResponse` object.
4. `TokenManager` extracts the token and its expiration (either from `expiresAtEpochSeconds` or by parsing the JWT payload).
5. The JWT is cached and added as an `Authorization: Bearer <jwt>` header to all subsequent requests.
6. Before any request, if the cached token is nearing expiration, it is automatically refreshed.

## Dependencies
- `com.fasterxml.jackson.core:jackson-databind`: For JSON serialization/deserialization.
- `org.openapitools:openapi-generator-maven-plugin`: For generating DTOs from OpenAPI specs.
- `org.btuk.http.exception.BTUKApiException`: Custom exception for API errors.
- `org.projectlombok:lombok`: For reducing boilerplate code.
- `java.net.http.HttpClient`: Standard Java 11+ HTTP client.
