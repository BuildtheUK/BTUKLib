package org.btuk.geography.projection;

/**
 * Standard projection interface for converting between geographic (longitude, latitude)
 * and projected (x, y/z) coordinates.
 */
public interface GeographicProjection {

    /**
     * Converts projected coordinates (e.g. Minecraft X, Z) to geographic coordinates (longitude, latitude in degrees).
     *
     * @param x projected X coordinate
     * @param y projected Y (or Minecraft Z) coordinate
     * @return array of [longitude, latitude] in degrees
     * @throws OutOfProjectionBoundsException if the point is outside the projection range
     */
    double[] toGeo(double x, double y) throws OutOfProjectionBoundsException;

    /**
     * Converts geographic coordinates (longitude, latitude in degrees) to projected coordinates (e.g. Minecraft X, Z).
     *
     * @param longitude longitude in degrees
     * @param latitude  latitude in degrees
     * @return array of [x, y] projected coordinates
     * @throws OutOfProjectionBoundsException if the point is outside the projection range
     */
    double[] fromGeo(double longitude, double latitude) throws OutOfProjectionBoundsException;

    /**
     * Returns the bounding box of projected coordinates: [minX, minY, maxX, maxY].
     */
    double[] bounds();

    /**
     * Indicates whether the projection is oriented upright.
     */
    boolean upright();

    /**
     * Scale factor of meters per projected unit.
     */
    double metersPerUnit();
}
