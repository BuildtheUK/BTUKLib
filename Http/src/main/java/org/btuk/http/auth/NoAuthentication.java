package org.btuk.http.auth;

import java.net.http.HttpRequest;
import java.util.concurrent.CompletableFuture;

/**
 * An authentication strategy that does nothing.
 */
public class NoAuthentication implements Authentication {
    @Override
    public CompletableFuture<HttpRequest.Builder> apply(HttpRequest.Builder requestBuilder) {
        return CompletableFuture.completedFuture(requestBuilder);
    }
}
