package org.btuk.http.auth;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class ApiCredentials {
    private final UUID clientId;
    private final UUID keyId;
    private final String secret;
}
