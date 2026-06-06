package org.btuk.http.auth;

import java.net.http.HttpRequest;
import java.util.concurrent.CompletableFuture;

/**
 * Interface for HTTP authentication strategies.
 */
public interface Authentication {
    /**
     * Applies authentication to the request builder.
     * @param requestBuilder The request builder to modify.
     * @return A future that completes when authentication has been applied.
     */
    CompletableFuture<HttpRequest.Builder> apply(HttpRequest.Builder requestBuilder);

    /**
     * Constant for no authentication.
     */
    Authentication NONE = new NoAuthentication();
}
