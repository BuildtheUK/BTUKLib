# Functional Documentation: Http Module

The `Http` module simplifies interaction with the BTUK REST API by automating authentication and providing a clean interface for common REST operations.

## Getting Started

To use the HTTP client, you need to provide your server's API credentials. These are typically provisioned per server.

### 1. Initialize the Client

#### With Bearer Authentication (Recommended)
```java
ApiCredentials credentials = ApiCredentials.builder()
        .clientId(UUID.fromString("your-client-uuid"))
        .keyId(UUID.fromString("your-key-uuid"))
        .secret("your-bcrypt-secret")
        .build();

BTUKHttpClient client = new BTUKHttpClient("http://localhost:8080", credentials);
```

#### With Custom Token Path
If your authentication endpoint is not the default `/api/auth/client/token`, you can specify it:
```java
BTUKHttpClient client = new BTUKHttpClient("http://localhost:8080", "/api/custom/token", credentials);
```

#### With No Authentication
```java
BTUKHttpClient client = new BTUKHttpClient("http://localhost:8080", Authentication.NONE);
```

#### Custom Authentication
You can implement the `Authentication` interface for custom needs (e.g., Basic Auth, custom headers).

### 2. Making API Calls

All calls are asynchronous and return a `CompletableFuture`.

#### Static Request Specifications (Best Practice)
Using `HttpRequestSpec` allows you to define requests cleanly and reuse them.

```java
// Define a GET request spec
HttpRequestSpec<Region[]> getRegions = HttpRequestSpec.get("/api/regions", Region[].class);

// Execute it
client.send(getRegions).thenAccept(regions -> { ... });

// With query parameters
HttpRequestSpec<Region> getRegion = HttpRequestSpec.<Region>builder()
        .method("GET")
        .path("/api/regions/find")
        .queryParameter("name", "London")
        .responseType(Region.class)
        .build();

client.send(getRegion);

// POST with fluent body parameters
HttpRequestSpec<Region> createRegion = HttpRequestSpec.<Region>builder()
        .method("POST")
        .path("/api/regions")
        .bodyParameter("name", "New Region")
        .bodyParameter("description", "Created via Fluent API")
        .responseType(Region.class)
        .build();

client.send(createRegion);
```

#### Direct Method Calls
For simple cases, you can use the direct methods:

##### GET Request
```java
client.get("/api/regions", Region[].class)
    .thenAccept(regions -> {
        for (Region region : regions) {
            System.out.println(region.getName());
        }
    })
    .exceptionally(ex -> {
        ex.printStackTrace();
        return null;
    });
```

#### POST Request
```java
Region newRegion = new Region("New Region");
client.post("/api/regions", newRegion, Region.class)
    .thenAccept(savedRegion -> System.out.println("Saved with ID: " + savedRegion.getId()));
```

#### DELETE Request
```java
client.delete("/api/regions/123")
    .thenRun(() -> System.out.println("Deleted successfully"));
```

## Features

- **Automated Authentication**: No need to manually handle tokens. The client performs the initial login and attaches the JWT to every request.
- **Token Lifecycle Management**: The client monitors token expiration and refreshes it automatically before it expires, ensuring uninterrupted service.
- **JSON Mapping**: Automatically converts Java objects to JSON for requests and maps JSON responses back to Java objects using Jackson.
- **Modern Java Networking**: Built on top of `java.net.http.HttpClient`, providing a lightweight, non-blocking experience.

## Error Handling

If a request fails (e.g., 4xx or 5xx status codes), the `CompletableFuture` will complete exceptionally with a `BTUKApiException`. This exception provides access to the HTTP status code and, if available, a structured `ErrorResponse` containing a standardized error code and optional parameters for localization.

### Handling Error Codes and Localization
The `BTUKApiException` provides helper methods to access the error details:

```java
client.get("/api/regions/invalid", Region.class)
    .exceptionally(ex -> {
        if (ex.getCause() instanceof BTUKApiException) {
            BTUKApiException apiEx = (BTUKApiException) ex.getCause();
            System.err.println("Status: " + apiEx.getStatusCode());
            
            String errorCode = apiEx.getErrorCode(); // e.g., "REGION_NOT_FOUND"
            List<String> params = apiEx.getParameters(); // e.g., ["123"]

            if (errorCode != null) {
                // You can use this for translation
                // String localizedMessage = myTranslator.translate(errorCode, params);
                // System.err.println(localizedMessage);
            }
        }
        return null;
    });
```
