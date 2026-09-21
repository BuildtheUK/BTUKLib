package org.btuk.worldguard;

public class WorldGuardException extends RuntimeException {
    public WorldGuardException(String message) {
        super(message);
    }

    public WorldGuardException(String message, Throwable cause) {
        super(message, cause);
    }
}
