package org.btuk.geography;

import org.btuk.geography.projection.BTEProjection;
import org.btuk.geography.projection.OutOfProjectionBoundsException;

/**
 * Placeholder for bi-directional conversion between Minecraft coordinates and real-life coordinates.
 */
public class CoordinateConverter {

    private CoordinateConverter() {}

    public static Coordinate toRealLife(MinecraftCoordinate mc) throws OutOfProjectionBoundsException {
        double[] geoCoords = BTEProjection.toGeoCoordinates(mc.x(), mc.z());
        return new Coordinate(geoCoords[1], geoCoords[0]);
    }

    public static MinecraftCoordinate toMinecraft(Coordinate real) throws OutOfProjectionBoundsException {
        double[] minecraftCoords = BTEProjection.fromGeoCoordinates(real.longitude(), real.latitude());
        return new MinecraftCoordinate(minecraftCoords[0], minecraftCoords[1]);
    }

    /**
     * Calculates the real-world distance in metres between two geographic coordinates.
     *
     * @param from starting coordinate
     * @param to   destination coordinate
     * @return distance in metres
     */
    public static double distance(Coordinate from, Coordinate to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Coordinates cannot be null");
        }
        return from.distanceTo(to);
    }

    /**
     * Calculates the real-world distance in metres between two Minecraft coordinates
     * using the real-world projection.
     *
     * @param from starting Minecraft coordinate
     * @param to   destination Minecraft coordinate
     * @return distance in metres
     * @throws OutOfProjectionBoundsException if either coordinate is outside projection bounds
     */
    public static double distance(MinecraftCoordinate from, MinecraftCoordinate to) throws OutOfProjectionBoundsException {
        Coordinate realFrom = toRealLife(from);
        Coordinate realTo = toRealLife(to);
        return realFrom.distanceTo(realTo);
    }
}
