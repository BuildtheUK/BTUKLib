package org.btuk.geography;

public record Coordinate(double latitude, double longitude) {

    private static final double EARTH_RADIUS_METRES = 6371000.0;

    // Cache key: rounded to 6 decimal places to avoid precision issues.
    public String getCacheKey() {
        return String.format("%.6f_%.6f", latitude, longitude);
    }

    /**
     * Calculates the great-circle distance in metres between this coordinate and another coordinate using the Haversine formula.
     *
     * @param other the target coordinate
     * @return distance in metres
     */
    public double distanceTo(Coordinate other) {
        if (other == null) {
            throw new IllegalArgumentException("Coordinate cannot be null");
        }
        double lat1Rad = Math.toRadians(this.latitude);
        double lon1Rad = Math.toRadians(this.longitude);
        double lat2Rad = Math.toRadians(other.latitude);
        double lon2Rad = Math.toRadians(other.longitude);

        double dLat = lat2Rad - lat1Rad;
        double dLon = lon2Rad - lon1Rad;

        double sinHalfLat = Math.sin(dLat / 2.0);
        double sinHalfLon = Math.sin(dLon / 2.0);

        double a = sinHalfLat * sinHalfLat + Math.cos(lat1Rad) * Math.cos(lat2Rad) * sinHalfLon * sinHalfLon;
        double c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));

        return EARTH_RADIUS_METRES * c;
    }
}
