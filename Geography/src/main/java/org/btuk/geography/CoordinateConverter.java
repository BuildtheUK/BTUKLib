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
}
