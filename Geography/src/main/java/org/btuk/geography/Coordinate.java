package org.btuk.geography;

public record Coordinate(double latitude, double longitude) {

    // Cache key: rounded to 6 decimal places to avoid precision issues.
    public String getCacheKey() {
        return String.format("%.6f_%.6f", latitude, longitude);
    }
}
