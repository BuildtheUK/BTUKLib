package org.btuk.geography.projection;

/**
 * Exception thrown when coordinate conversions fall outside projection bounds.
 */
public final class OutOfProjectionBoundsException extends Exception {
    private static final OutOfProjectionBoundsException INSTANCE = new OutOfProjectionBoundsException();

    public static OutOfProjectionBoundsException get() {
        return INSTANCE;
    }

    public static void checkInRange(double x, double y, double maxX, double maxY) throws OutOfProjectionBoundsException {
        if (Math.abs(x) > maxX || Math.abs(y) > maxY) {
            throw OutOfProjectionBoundsException.get();
        }
    }

    public static void checkLongitudeLatitudeInRange(double longitude, double latitude) throws OutOfProjectionBoundsException {
        checkInRange(longitude, latitude, 180, 90);
    }

    public OutOfProjectionBoundsException() {
        super("Coordinates out of projection bounds");
    }

    public OutOfProjectionBoundsException(String message) {
        super(message);
    }
}
